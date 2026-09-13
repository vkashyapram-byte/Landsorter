package com.govtech.landstack.util

import android.content.Context
import android.content.Intent
import com.govtech.landstack.MainActivity
import kotlin.system.exitProcess

class GlobalExceptionHandler(
    private val applicationContext: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, exception: Throwable) {
        exception.printStackTrace()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("show_error_screen", true)
            putExtra("error_message", exception.localizedMessage ?: "Unknown Error")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        applicationContext.startActivity(intent)
        
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(10)
    }
}
