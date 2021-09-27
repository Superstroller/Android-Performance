package com.supertramp.performance

import android.os.Trace

fun traceBegin(name : String) {
    Trace.beginSection(name)
}

fun traceEnd() {
    Trace.endSection()
}