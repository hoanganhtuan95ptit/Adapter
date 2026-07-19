package com.simple.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.simple.adapter.base.BaseBindingViewHolder

abstract class ViewItemAdapter<VI : ViewItem, VB : ViewBinding>() : ViewItemAdapterDelegate {

    abstract override val viewItemClass: Class<VI>


    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<VB> {

        val viewHolder = BaseBindingViewHolder(createViewBinding(LayoutInflater.from(parent.context), parent, viewType), viewType)

        viewHolder.itemView.setOnSafeClickListener {

            val viewItem = (viewHolder.getViewItem() as? VI) ?: return@setOnSafeClickListener

            onViewItemClick(viewItem)
        }

        return viewHolder
    }

    abstract fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): VB


    @Suppress("UNCHECKED_CAST")
    final override fun bind(binding: ViewBinding, viewType: Int, position: Int, item: ViewItem, payloads: List<String>) {

        onBindViewHolder(binding as VB, viewType, position, item as VI, payloads)
    }

    open protected fun onViewItemClick(item: VI) {
    }

    open protected fun onBindViewHolder(binding: VB, viewType: Int, position: Int, item: VI, payloads: List<String>) {
    }

    private fun View.setOnSafeClickListener(onSafeClick: (View) -> Unit) {

        var lastTimeClicked: Long = 0

        setOnClickListener {

            if (System.currentTimeMillis() - lastTimeClicked < 1000) return@setOnClickListener
            lastTimeClicked = System.currentTimeMillis()

            onSafeClick(it)
        }
    }

    protected fun RecyclerView.ViewHolder.getViewItem(): ViewItem? {

        return (bindingAdapter as? MultiAdapter)?.currentList?.getOrNull(absoluteAdapterPosition)
    }
}
