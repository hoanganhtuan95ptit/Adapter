package com.simple.adapter.def

import androidx.recyclerview.widget.DiffUtil
import com.simple.adapter.ViewItem

internal class DefaultItemCallback<T : ViewItem> : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem::class == newItem::class && oldItem.areItemsTheSame() == newItem.areItemsTheSame()
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {

        val oldCompare = oldItem.getContentsCompare()
        val newCompare = newItem.getContentsCompare()

        if (oldCompare.size != newCompare.size) return false

        for (i in oldCompare.indices) {
            if (oldCompare[i].first != newCompare[i].first) return false
        }

        return true
    }

    override fun getChangePayload(oldItem: T, newItem: T): Any? {

        val payloads = mutableListOf<Any>()

        val oldCompare = oldItem.getContentsCompare()
        val newCompare = newItem.getContentsCompare()

        oldCompare.forEachIndexed { index, pair ->
            val newVal = newCompare.getOrNull(index)?.first
            if (pair.first != newVal) {
                payloads.add(pair.second)
            }
        }

        return payloads.ifEmpty {
            super.getChangePayload(oldItem, newItem)
        }
    }
}