package com.supertramp.performance

import android.os.Looper
import android.os.Trace

private var beginCount = 0
private var mainThreadId = 0L

fun traceBegin(name : String) {
    if (mainThreadId == 0L && Looper.myLooper() == Looper.getMainLooper()) {
        mainThreadId = Thread.currentThread().id
    }
    else if (mainThreadId != Thread.currentThread().id) {
        return
    }
    beginCount ++
    Trace.beginSection(name)
}

fun traceEnd() {
    if (mainThreadId == Thread.currentThread().id) {
        beginCount --
        if (beginCount < 0) {
            beginCount = 0
        }
        Trace.endSection()
    }
}

fun catchIn() {
    if (mainThreadId == Thread.currentThread().id) {
        while (beginCount > 1) {
            traceEnd()
        }
    }
}

fun onlineTraceBegin(name : String) {

}

fun onlineTraceEnd() {

}