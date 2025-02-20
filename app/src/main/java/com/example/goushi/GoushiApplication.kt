package com.example.goushi

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GoushiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 设置全局未捕获异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("GoushiApp", "Uncaught exception in thread ${thread.name}", throwable)
            // 可以在这里添加崩溃报告逻辑
        }
    }
} 