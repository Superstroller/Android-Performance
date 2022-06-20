package com.supertramp.performance

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.tv_button_01)?.setOnClickListener {
            Thread.sleep(500)
            Toast.makeText(this, "Hello world", Toast.LENGTH_SHORT).show()
        }
        findViewById<TextView>(R.id.tv_button_02)?.setOnClickListener {
            catchException()
        }
        findViewById<TextView>(R.id.tv_button_03)?.setOnClickListener {
            startActivity(Intent(this, BActivity::class.java))
        }
    }

    private fun catchException() {
        try {
            throwException()
        }catch (e: Exception){}
    }

    private fun throwException() {
        Thread.sleep(200)
        throwException01()
    }

    private fun throwException01() {
        Thread.sleep(200)
        throwException02()
    }

    private fun throwException02() {
        Thread.sleep(200)
        throwException03()
    }

    private fun throwException03() {
        Thread.sleep(200)
        error()
    }

    private fun error() {
        throw NullPointerException()
    }

}