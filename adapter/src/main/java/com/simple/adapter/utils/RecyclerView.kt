package com.simple.adapter.utils

import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionManager
import com.simple.adapter.MultiAdapter
import com.simple.adapter.ViewItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

// Dùng -1 làm sentinel value để đánh dấu toàn bộ list thay đổi (onChanged)
private const val FULL_CHANGE_MARKER = -1

/**
 * Submit danh sách mới vào adapter và suspend cho đến khi DiffUtil xử lý xong.
 *
 * @param viewItemList Danh sách ViewItem mới cần hiển thị.
 * @param adapterList  Danh sách adapter class name hỗ trợ render các ViewItem.
 * @param isAnimation  Nếu true, chạy AutoTransition cho các item thay đổi visible trên màn hình.
 * @param ignoreTransitionViewId Danh sách view id sẽ bị loại khỏi hiệu ứng transition.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("UNCHECKED_CAST")
suspend fun RecyclerView.submitListAndAwait(
    viewItemList: List<ViewItem>,
    adapterList: List<String>,
    isAnimation: Boolean = false,
    ignoreTransitionViewId: List<Int> = emptyList()
) {
    val adapter = adapter as? MultiAdapter ?: return

    if (!isAnimation) suspendCancellableCoroutine<Unit> { continuation ->

        // Không cần animation — chỉ submit và resume khi DiffUtil callback xong
        adapter.submitList(viewItemList, adapterList) {
            if (continuation.isActive) continuation.resume(Unit, onCancellation = null)
        }
    } else suspendCancellableCoroutine<Unit> { continuation ->

        val changedPositions = mutableSetOf<Int>()

        // Theo dõi những position nào thay đổi để kiểm tra có cần chạy transition không
        val observer = createChangedPositionsObserver(changedPositions)
        adapter.registerAdapterDataObserver(observer)

        val transition = createTransition(ignoreTransitionViewId)

        // Resume coroutine khi transition kết thúc (end hoặc cancel)
        transition.addListener(createTransitionListener {
            if (continuation.isActive) continuation.resume(Unit, onCancellation = null)
        })

        // Phải gọi beginDelayedTransition trước submitList để TransitionManager
        // capture được trạng thái before/after của các view
        TransitionManager.beginDelayedTransition(this, transition)

        adapter.submitList(viewItemList, adapterList) {

            adapter.unregisterAdapterDataObserver(observer)

            // Nếu không có thay đổi nào visible trên màn hình thì bỏ qua transition,
            // resume luôn để tránh treo coroutine chờ animation không bao giờ xảy ra
            if (!hasVisibleChange(changedPositions)) {
                TransitionManager.endTransitions(this)
                if (continuation.isActive) continuation.resume(Unit, onCancellation = null)
            }
        }

        // Dọn dẹp transition nếu coroutine bị cancel từ bên ngoài
        continuation.invokeOnCancellation {
            TransitionManager.endTransitions(this@submitListAndAwait)
        }
    }
}

// Tạo AutoTransition và loại trừ các view id không muốn animate
private fun createTransition(ignoreViewIds: List<Int>): Transition {
    return AutoTransition().apply {
        ignoreViewIds.forEach { excludeTarget(it, true) }
    }
}

// Extension giúp add một dải position vào set thay vì loop thủ công
private fun MutableSet<Int>.addRange(positionStart: Int, itemCount: Int) {
    addAll(positionStart until positionStart + itemCount)
}

// Observer theo dõi tất cả position bị thay đổi bởi DiffUtil
private fun createChangedPositionsObserver(changedPositions: MutableSet<Int>): RecyclerView.AdapterDataObserver {
    return object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            changedPositions.addRange(positionStart, itemCount)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            changedPositions.addRange(positionStart, itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            changedPositions.addRange(positionStart, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            changedPositions.add(fromPosition)
            changedPositions.add(toPosition)
        }

        override fun onChanged() {
            // Toàn bộ dataset thay đổi — đánh dấu bằng FULL_CHANGE_MARKER
            changedPositions.add(FULL_CHANGE_MARKER)
        }
    }
}

// Listener gọi onDone() khi transition kết thúc hoặc bị cancel,
// đồng thời tự remove để tránh callback nhiều lần
private fun createTransitionListener(onDone: () -> Unit): Transition.TransitionListener {
    return object : TransitionListenerAdapter() {
        override fun onTransitionEnd(transition: Transition) {
            transition.removeListener(this)
            onDone()
        }

        override fun onTransitionCancel(transition: Transition) {
            transition.removeListener(this)
            onDone()
        }
    }
}

// Kiểm tra xem có position nào trong changedPositions đang visible trên màn hình không
private fun RecyclerView.hasVisibleChange(changedPositions: Set<Int>): Boolean {
    // FULL_CHANGE_MARKER nghĩa là toàn bộ list đổi — luôn coi là có visible change
    if (changedPositions.contains(FULL_CHANGE_MARKER)) return true

    val visiblePositions = buildSet {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val pos = getChildAdapterPosition(child)
            if (pos != RecyclerView.NO_POSITION) add(pos)
        }
    }
    return changedPositions.any { it in visiblePositions }
}