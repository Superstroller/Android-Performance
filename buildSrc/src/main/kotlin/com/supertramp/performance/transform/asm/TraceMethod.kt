package com.supertramp.performance.transform.asm

import java.lang.StringBuilder

class TraceMethod(private var className : String? = null,
                  private var name : String? = null,
                  private var desc : String? = null) {

    fun getSimpleName() : String? {
        return name
    }

    fun getFullName() : String? {
        val builder = StringBuilder()
        builder.append(className)
        builder.append(name)
        builder.append(desc)
        return if (builder.isNotEmpty()) builder.toString() else null
    }

}