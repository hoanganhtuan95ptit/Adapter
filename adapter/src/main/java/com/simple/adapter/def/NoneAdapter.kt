package com.simple.adapter.def

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapterProvider
import com.simple.adapter.register.databinding.AdapterItemNoneBinding
import com.simple.auto.register.AutoRegister

@Adapter
class NoneAdapter : com.simple.adapter.ViewItemAdapter<NoneViewItem, AdapterItemNoneBinding>() {

    override val viewItemClass: Class<NoneViewItem> by lazy {
        NoneViewItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): AdapterItemNoneBinding {
        return AdapterItemNoneBinding.inflate(layoutInflater, parent, false)
    }
}

data class NoneViewItem(val id: String = "") : com.simple.adapter.ViewItem {

    override fun areItemsTheSame(): List<Any> = listOf(id)
}