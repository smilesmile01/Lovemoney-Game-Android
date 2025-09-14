package com.lovemoney.lovemoney.utils

import android.util.Log

object Logger {
    private const val TAG = "LoveMoneyGame"

    // Debug logs
    fun d(message: String, tag: String = TAG) {
        Log.d("[$tag]", message)
    }

    // Info logs
    fun i(message: String, tag: String = TAG) {
        Log.i("[$tag]", message)
    }

    // Warning logs
    fun w(message: String, tag: String = TAG) {
        Log.w("[$tag]", message)
    }

    // Error logs
    fun e(message: String, tag: String = TAG, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e("[$tag]", message, throwable)
        } else {
            Log.e("[$tag]", message)
        }
    }

    // Verbose logs
    fun v(message: String, tag: String = TAG) {
        Log.v("[$tag]", message)
    }

    // WebView specific logs
    fun webView(message: String) {
        Log.d("[WebView]", message)
    }

    // Network related logs
    fun network(message: String) {
        Log.d("[Network]", message)
    }

    // Lifecycle logs
    fun lifecycle(activityName: String, method: String) {
        Log.d("[Lifecycle]", "$activityName -> $method")
    }

    // Performance logs
    fun performance(message: String) {
        Log.d("[Performance]", message)
    }

    // JS interface logs
    fun jsInterface(message: String) {
        Log.d("[JSInterface]", message)
    }

    // System info logs
    fun system(message: String) {
        Log.d("[System]", message)
    }

    // Print detailed exception stack trace
    fun logException(tag: String, message: String, exception: Throwable) {
        Log.e("[$tag]", "$message: ${exception.message}")
        Log.e("[$tag]", "Stack trace:", exception)
    }

    // Print application startup info
    fun logAppStart() {
        i("========== LoveMoney App Starting ==========")
        system("Android Version: ${android.os.Build.VERSION.RELEASE}")
        system("SDK Version: ${android.os.Build.VERSION.SDK_INT}")
        system("Device Model: ${android.os.Build.MODEL}")
        system("Device Brand: ${android.os.Build.BRAND}")
        i("==========================================")
    }
}