package com.simple.adapter.utils

import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapterProvider
import com.simple.auto.register.AutoRegisterManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val adapterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

val adapterStateFlow: StateFlow<List<String>> = AutoRegisterManager
    .subscribe(ViewItemAdapterProvider::class.java)
    .map { providers -> providers.flatMap { it.provider() } }
    .stateIn(adapterScope, SharingStarted.Eagerly, emptyList())

fun Flow<List<ViewItem>>.attachAdapter() = combine(this, adapterStateFlow) { viewItemList, adapterList ->

    viewItemList to adapterList
}