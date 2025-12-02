package com.tenjin.sdk

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AdvertisingIdProvider {
    suspend fun getAdvertisingId(): String?
}

class DefaultAdvertisingIdProvider(private val appContext: Context) : AdvertisingIdProvider {
    override suspend fun getAdvertisingId(): String? = withContext(Dispatchers.IO) {
        try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(appContext)
            adInfo.id
        } catch (e: Exception) {
            Logger.e("AdvertisingIdProvider", "Failed to get advertising ID.", e)
            null
        }
    }
}
