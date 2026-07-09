package com.simple.adapter

import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.simple.adapter.base.BaseAsyncAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.adapter.def.NoneAdapter

class MultiAdapter() : BaseAsyncAdapter<ViewItem, ViewBinding>() {

    private val adapterClassNames = ArrayList<String>()

    private val typeAndAdapter = HashMap<Int, ViewItemAdapterDelegate>()
    private val viewItemClassAndType = HashMap<String, Int>()


    init {

        registerAdapter(NoneAdapter::class.java.name)
    }


    fun submitList(list: List<ViewItem>, adapter: List<String> = emptyList(), commitCallback: Runnable? = null) {
        registerAdapters(adapter)
        super.submitList(list.filter { it.javaClass.name in viewItemClassAndType.keys }, commitCallback)  // to
    }

    private fun registerAdapters(adapter: List<String>) {
        adapter.filter { it !in adapterClassNames }.forEach(::registerAdapter)
    }

    private fun registerAdapter(className: String) = runCatching {

        val delegate = Class.forName(className).getDeclaredConstructor().newInstance() as? ViewItemAdapterDelegate ?: return@runCatching

        adapterClassNames.add(className)

        val viewType = adapterClassNames.size
        typeAndAdapter[viewType] = delegate
        viewItemClassAndType[delegate.viewItemClass.name] = viewType
    }

    override fun submitList(list: List<ViewItem?>?) {
        error("use  fun submitList(list: List<ViewItem?>?, adapter: List<String> = emptyList(), commitCallback: Runnable?)")
    }

    override fun submitList(list: List<ViewItem?>?, commitCallback: Runnable?) {
        error("use  fun submitList(list: List<ViewItem?>?, adapter: List<String> = emptyList(), commitCallback: Runnable?)")
    }


    override fun getItemViewType(position: Int): Int {

        val itemClassName = getItem(position)?.javaClass?.name.orEmpty()

        return viewItemClassAndType[itemClassName] ?: error("not found impl viewtype for ${itemClassName}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ViewBinding> {
        return typeAndAdapter[viewType]?.createViewHolder(parent, viewType) ?: error("not found impl viewModel for viewType")
    }


    override fun onViewAttachedToWindow(holder: BaseBindingViewHolder<ViewBinding>) {
        typeAndAdapter[holder.viewType]?.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: BaseBindingViewHolder<ViewBinding>) {
        typeAndAdapter[holder.viewType]?.onViewDetachedFromWindow(holder)
    }


    override fun bind(binding: ViewBinding, viewType: Int, position: Int, item: ViewItem, payloads: List<String>) {
        typeAndAdapter[viewType]?.bind(binding, viewType, position, item, payloads)
    }
}
