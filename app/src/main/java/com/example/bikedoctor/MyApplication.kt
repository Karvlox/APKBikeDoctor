// app/src/main/java/com/example/bikedoctor/MyApplication.kt
package com.example.bikedoctor

import android.app.Application
import android.content.Context

class MyApplication : Application() {
    companion object {
        lateinit var instance: MyApplication
        val context: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}