package com.supertramp.performance

import android.os.Looper
import android.os.Trace
import android.util.Log

private var beginCount = 0
private var beginNames = arrayOfNulls<String>(10)
private var mainThreadId = 0L
var enableLog = false

fun traceBegin(name : String) {
    if (mainThreadId == 0L && Looper.myLooper() == Looper.getMainLooper()) {
        mainThreadId = Thread.currentThread().id
    }
    else if (mainThreadId != Thread.currentThread().id) {
        return
    }
    if (beginCount < 10) {
        beginNames[beginCount] = name
    }
    beginCount ++
    Trace.beginSection(name)
    if (enableLog) {
        Log.i("atrace b", "$beginCount $name")
    }
}

fun traceEnd() {
    if (mainThreadId == Thread.currentThread().id) {
        if (beginCount <= 0) {
            return
        }
        Trace.endSection()
        beginCount --
        if (beginCount < 10) {
            beginNames[beginCount] = null
        }
        if (enableLog) {
            Log.i("atrace e", "$beginCount")
        }
    }
}

fun catchIn(name : String) {
    if (mainThreadId == Thread.currentThread().id) {
        while (beginCount >= 10 || (beginCount > 0 && beginNames[beginCount - 1] != name)) {
            traceEnd()
        }
    }
}