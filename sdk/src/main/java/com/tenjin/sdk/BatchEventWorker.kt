package com.tenjin.sdk

import android.content.Context
import android.content.SharedPreferences
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class BatchEventWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val eventDao: EventDao,
    private val userDao: UserDao,
    private val tenjinApi: TenjinApi,
    private val advertisingIdProvider: AdvertisingIdProvider
) : CoroutineWorker(appContext, workerParams) {
    private val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences(
        "tenjin_sdk_prefs",
        Context.MODE_PRIVATE
    )

    override suspend fun doWork(): Result {
        val events = eventDao.getEvents()
        if (events.isEmpty()) {
            Logger.d("BatchEventWorker", "No events to send.")
            return Result.success()
        }

        Logger.d("BatchEventWorker", "Attempting to send ${events.size} events individually.")

        val advertisingId = advertisingIdProvider.getAdvertisingId()
        if (advertisingId == null) {
            Logger.e("BatchEventWorker", "Failed to get advertising ID. Retrying later.")
            return Result.retry()
        }

        val username = userDao.getUsername()?.username
        val apiKey = sharedPreferences.getString("api_key", null)
        val bundleId = sharedPreferences.getString("bundle_id", null)

        if (apiKey == null || bundleId == null) {
            Logger.e("BatchEventWorker", "API key or bundle ID is not set. Aborting.")
            return Result.failure()
        }

        for (event in events) {
            val params = mutableMapOf(
                "bundle_id" to bundleId,
                "api_key" to apiKey,
                "platform" to "android",
                "event" to event.eventName,
                "advertising_id" to advertisingId
            )
            username?.let { params["username"] = it }

            try {
                val response = tenjinApi.sendEvent(params)
                if (response.isSuccessful) {
                    Logger.d("BatchEventWorker", "Event '${event.eventName}' sent successfully. Deleting from queue.")
                    eventDao.deleteEvent(event.id)
                } else {
                    Logger.e("BatchEventWorker", "Failed to send event '${event.eventName}'. Server responded with ${response.code()}. Retrying later.")
                    return Result.retry() // Retry the whole work if one event fails
                }
            } catch (e: Exception) {
                Logger.e("BatchEventWorker", "Failed to send event '${event.eventName}'. ${e.message}. Retrying later.", e)
                return Result.retry() // Retry the whole work if one event fails
            }
        }

        Logger.d("BatchEventWorker", "All events processed in this batch.")
        return Result.success()
    }
}