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
            mJob?.cancel()
        }
    }

}