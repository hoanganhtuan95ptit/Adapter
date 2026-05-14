package com.simple.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.simple.adapter.base.BaseBindingViewHolder

abstract class ViewItemAdapter<VI : ViewItem, VB : ViewBinding>() : ViewItemAdapterDelegate {

    abstract override val viewItemClass: Class<VI>


    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<VB> {

        val viewHolder = BaseBindingViewHolder(createViewBinding(LayoutInflater.from(parent.context), parent, viewType), viewType)

        return viewHolder
    }

    abstract fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): VB


    @Suppress("UNCHECKED_CAST")
    final override fun bind(binding: ViewBinding, viewType: Int, position: Int, item: ViewItem, payloads: List<String>) {

        onBindViewHolder(binding as VB, viewType, position, item as VI, payloads)
    }


    open fun onBindViewHolder(binding: VB, viewType: Int, position: Int, item: VI, payloads: List<String>) {
    }
}
