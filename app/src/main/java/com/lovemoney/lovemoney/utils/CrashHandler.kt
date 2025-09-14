package com.lovemoney.lovemoney.utils

import android.content.Context
import android.os.Process
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {

    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private var mContext: Context? = null

    companion object {
        @Volatile
        private var instance: CrashHandler? = null

        fun getInstance(): CrashHandler {
            return instance ?: synchronized(this) {
                instance ?: CrashHandler().also { instance = it }
            }
        }
    }

    fun init(context: Context) {
        mContext = context
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        Logger.i("CrashHandler initialized", "CrashHandler")
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        Logger.e("Uncaught Exception Occurred!", "CrashHandler", ex)

        // Save crash log to file
        saveCrashInfo(ex)

        // Print detailed info
        printCrashInfo(thread, ex)

        // Give system some time to save logs
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            Logger.e("Sleep interrupted", "CrashHandler", e)
        }

        // Call system default exception handler
        mDefaultHandler?.uncaughtException(thread, ex) ?: run {
            Process.killProcess(Process.myPid())
            exitProcess(1)
        }
    }

    private fun saveCrashInfo(ex: Throwable) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "crash_$timestamp.log"

            mContext?.let { context ->
                val crashDir = File(context.externalCacheDir, "crashes")
                if (!crashDir.exists()) {
                    crashDir.mkdirs()
                }

                val crashFile = File(crashDir, fileName)
                FileOutputStream(crashFile).use { fos ->
                    val crashInfo = buildString {
                        appendLine("========== CRASH REPORT ==========")
                        appendLine("Time: $timestamp")
                        appendLine("Thread: ${Thread.currentThread().name}")
                        appendLine("Device: ${android.os.Build.BRAND} ${android.os.Build.MODEL}")
                        appendLine("Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                        appendLine("App Version: 1.0.0")
                        appendLine("================================")
                        appendLine()
                        appendLine("Exception: ${ex.javaClass.simpleName}")
                        appendLine("Message: ${ex.message}")
                        appendLine()
                        appendLine("Stack Trace:")
                        appendLine(ex.stackTraceToString())
                        appendLine("================================")
                    }
                    fos.write(crashInfo.toByteArray())
                }
                Logger.i("Crash log saved to: ${crashFile.absolutePath}", "CrashHandler")
            }
        } catch (e: Exception) {
            Logger.e("Failed to save crash info", "CrashHandler", e)
        }
    }

    private fun printCrashInfo(thread: Thread, ex: Throwable) {
        Logger.e("========== CRASH REPORT ==========", "CrashHandler")
        Logger.e("Thread: ${thread.name}", "CrashHandler")
        Logger.e("Exception: ${ex.javaClass.simpleName}", "CrashHandler")
        Logger.e("Message: ${ex.message}", "CrashHandler")
        Logger.e("Stack trace:", "CrashHandler", ex)
        Logger.e("=================================", "CrashHandler")
    }
}