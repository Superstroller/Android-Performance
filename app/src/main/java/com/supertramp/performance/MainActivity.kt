package com.supertramp.performance

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import kotlinx.coroutines.*

class MainActivity : Activity() {

    private val applicationScope = CoroutineScope(SupervisorJob())
    private var mJob : Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.tv_button_01)?.setOnClickListener {
            mJob = applicationScope.launch(Dispatchers.Main) {
                delay(10000)
            }
        }
        findViewById<TextView>(R.id.tv_button_02)?.setOnClickListener {
            //mJob?.cancel()
            catchException()
        }
    }

    fun catchException() {
        try {
            Thread.sleep(200)
            throwException1()
        }catch (e : Exception) {}
    }

    fun throwException1() {
        Thread.sleep(200)
        throwException2()
    }

    fun throwException2() {
        Thread.sleep(200)
        throwException3()
    }

    fun throwException3() {
        Thread.sleep(200)
        error()
    }

    fun error() {
        throw NullPointerException()
    }

}