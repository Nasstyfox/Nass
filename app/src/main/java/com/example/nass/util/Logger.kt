package com.example.nass.util

import android.util.Log

/**
 * Tiny wrapper around android.util.Log so every message is prefixed with "Nass.".
 * Use Logcat filter "Nass" to see the full app trace during a demo.
 */
object Logger {
    private const val ROOT = "Nass"

    fun d(tag: String, message: String) = Log.d("$ROOT.$tag", message)
    fun i(tag: String, message: String) = Log.i("$ROOT.$tag", message)

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.w("$ROOT.$tag", message, throwable)
        else Log.w("$ROOT.$tag", message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.e("$ROOT.$tag", message, throwable)
        else Log.e("$ROOT.$tag", message)
    }
}