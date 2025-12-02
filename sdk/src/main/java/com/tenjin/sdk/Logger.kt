package com.tenjin.sdk

import android.util.Log

object Logger {
    var isEnabled = false

    fun d(tag: String, message: String) {
        if (isEnabled) {
            Log.d(tag, message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isEnabled) {
            Log.e(tag, message, throwable)
        }
    }
}