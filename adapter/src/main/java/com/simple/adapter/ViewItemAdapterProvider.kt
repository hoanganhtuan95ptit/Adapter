package com.simple.adapter

import com.simple.auto.register.AutoKeep

abstract class ViewItemAdapterProvider : AutoKeep {

    abstract fun provider(): List<String>
}
