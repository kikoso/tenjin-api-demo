package com.tenjin.sdk

import android.content.Context
import android.content.SharedPreferences
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    private val userDao = AppDatabase.getDatabase(applicationContext).userDao()
    private val eventDao = AppDatabase.getDatabase(applicationContext).eventDao()
    private val tenjinApi = RetrofitClient.instance
    private val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences(
        "tenjin_sdk_prefs",
        Context.MODE_PRIVATE
    )

    override suspend fun doWork(): Result {
        val events = eventDao.getEvents()
        if (events.isEmpty()) {
            return Result.success()
        }

        val advertisingId = getAdvertisingId()
        if (advertisingId == null) {
            // We can't send events without an advertising ID, so we retry later.
            return Result.retry()
        }
        val username = userDao.getUsername()?.username
        val apiKey = sharedPreferences.getString("api_key", null)

        if (apiKey == null) {
            // API key is not set, we can't send events.
            return Result.failure()
        }

        for (event in events) {
            val params = mutableMapOf(
                "bundle_id" to "com.tenjin.testapp",
                "api_key" to apiKey,
                "platform" to "android",
                "event" to event.eventName,
                "advertising_id" to advertisingId
            )
            username?.let { params["username"] = it }

            try {
                val response = tenjinApi.sendEvent(params)
                if (response.isSuccessful) {
                    eventDao.deleteEvent(event.id)
                } else {
                    return Result.retry()
                }
            } catch (e: Exception) {
                return Result.retry()
            }
        }

        return Result.success()
    }

    private suspend fun getAdvertisingId(): String? = withContext(Dispatchers.IO) {
        try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
            adInfo.id
        } catch (e: Exception) {
            null
        }
    }
}
