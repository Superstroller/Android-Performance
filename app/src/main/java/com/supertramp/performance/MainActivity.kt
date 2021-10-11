package com.supertramp.performance

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        oneLine()
        twoLine(1)
    }

    private fun oneLine() : Int {
        return 1
    }

    private fun twoLine(a : Int) : Int {
        val tmp = 1 + a
        return tmp
    }

}