package com.tenjin.sdk

data class TenjinSDKConfig(
    val apiKey: String,
    val bundleId: String? = null,
    val isLoggingEnabled: Boolean = false,
    val eventQueueSizeLimit: Int = 1000
)