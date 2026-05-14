package com.simple.adapter.base

import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.simple.adapter.def.DefaultItemCallback
import java.util.concurrent.Executors

/**
 * A pool for diffing tasks to avoid creating new threads repeatedly.
 */
private val diffExecutor by lazy {
    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors().coerceAtLeast(2))
}

class BaseBindingViewHolder<out B : ViewBinding>(
    val binding: B,
    val viewType: Int
) : RecyclerView.ViewHolder(binding.root)

abstract class BaseAsyncAdapter<T : com.simple.adapter.ViewItem, B : ViewBinding>(
    itemCallback: DiffUtil.ItemCallback<T>? = null,
) : androidx.recyclerview.widget.ListAdapter<T, BaseBindingViewHolder<B>>(
    AsyncDifferConfig.Builder(itemCallback ?: DefaultItemCallback())
        .setBackgroundThreadExecutor(diffExecutor)
        .build()
) {

    final override fun onBindViewHolder(p0: BaseBindingViewHolder<B>, p1: Int) {

    }

    final override fun onBindViewHolder(holder: BaseBindingViewHolder<B>, position: Int, payloads: MutableList<Any>) {

        val item = getItem(position) ?: return

        val flattenedPayloads = payloads
            .filterIsInstance<List<*>>()
            .flatten()
            .filterIsInstance<String>()

        bind(holder.binding, holder.viewType, position, item, flattenedPayloads)
    }

    abstract fun bind(binding: B, viewType: Int, position: Int, item: T, payloads: List<String>)
}
