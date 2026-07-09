package com.simple.adapter

import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.auto.register.AutoKeep

interface ViewItemAdapterDelegate : AutoKeep {

    val viewItemClass: Class<*>

    fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ViewBinding>

    fun onViewAttachedToWindow(holder: BaseBindingViewHolder<ViewBinding>) {
    }

    fun onViewDetachedFromWindow(holder: BaseBindingViewHolder<ViewBinding>) {
    }

    fun bind(binding: ViewBinding, viewType: Int, position: Int, item: ViewItem, payloads: List<String>)
}

