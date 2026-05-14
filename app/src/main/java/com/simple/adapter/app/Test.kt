package com.simple.adapter.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.simple.adapter.Adapter
import com.simple.adapter.MultiRecyclerView
import com.simple.adapter.ViewItemAdapterProvider
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.adapter.register.databinding.AdapterItemNoneBinding
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.auto.register.AutoRegisterManager
import kotlinx.coroutines.flow.map

@Adapter
class TestAdapter : com.simple.adapter.ViewItemAdapter<TestViewItem, AdapterItemNoneBinding>() {

    override val viewItemClass: Class<TestViewItem> by lazy {
        TestViewItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): AdapterItemNoneBinding {
        return AdapterItemNoneBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<AdapterItemNoneBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)

        viewHolder.itemView.setOnClickListener {

            val item = (viewHolder.bindingAdapter as ListAdapter<*, *>).currentList.getOrNull(viewHolder.absoluteAdapterPosition) as? TestViewItem ?: return@setOnClickListener

            // todo gửi sự kiện
        }

        return viewHolder
    }

    override fun onBindViewHolder(binding: AdapterItemNoneBinding, viewType: Int, position: Int, item: TestViewItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)

        if (payloads.isEmpty() || payloads.contains("text")) {
            // todo set text
        }
    }
}

data class TestViewItem(
    val id: String = "",
    val text: String = ""
) : com.simple.adapter.ViewItem {

    override fun areItemsTheSame(): List<Any> = listOf(id)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        text to "text"
    )
}


suspend fun test(fragment: androidx.fragment.app.Fragment) {

    val testList = arrayListOf(
        TestViewItem()
    )

    val recyclerView = MultiRecyclerView(fragment.requireContext())
    recyclerView.layoutManager = LinearLayoutManager(fragment.requireContext())

    AutoRegisterManager.subscribe(ViewItemAdapterProvider::class.java).map { it.flatMap { it.provider() } }.collect {

        recyclerView.submitListAndAwait(viewItemList = testList, adapterList = it, isAnimation = true)
    }
}

suspend fun testWithLivedata(fragment: androidx.fragment.app.Fragment) {

    val viewItemListLiveData = MutableLiveData(arrayListOf(TestViewItem()))

    val recyclerView = MultiRecyclerView(fragment.requireContext())
    recyclerView.layoutManager = LinearLayoutManager(fragment.requireContext())

    viewItemListLiveData.asFlow().attachAdapter().collect { (viewItemList, adapterList) ->

        recyclerView.submitListAndAwait(viewItemList = viewItemList, adapterList = adapterList, isAnimation = true)
    }
}