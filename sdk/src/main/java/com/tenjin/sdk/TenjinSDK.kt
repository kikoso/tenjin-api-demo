package com.tenjin.sdk

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.atomic.AtomicBoolean

class TenjinSDK internal constructor(
    private val appContext: Context,
    private val config: TenjinSDKConfig,
    private val eventDao: EventDao,
    private val userDao: UserDao,
    private val workManager: WorkManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val isInitialized = AtomicBoolean(false)
    fun initialize() {
        if (isInitialized.getAndSet(true)) {
            Logger.d(TAG, "TenjinSDK already initialized.")
            return
        }
        Logger.isEnabled = config.isLoggingEnabled

        val finalBundleId = config.bundleId ?: appContext.packageName
        // Store apiKey and bundleId in a secure way, for this example we use SharedPreferences
        val sharedPreferences = appContext.getSharedPreferences("tenjin_sdk_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putString("api_key", config.apiKey)
            putString("bundle_id", finalBundleId)
        }
        Logger.d(TAG, "TenjinSDK initialized with apiKey: ${config.apiKey} and bundleId: $finalBundleId")
    }

    fun setUsername(username: String) {
        if (!isInitialized.get()) {
            Logger.e(TAG, "TenjinSDK not initialized. Please call initialize() first.")
            return
        }
        coroutineScope.launch {
            userDao.setUsername(User(username = username))
            Logger.d(TAG, "Username set to: $username")
        }
    }

    suspend fun getUsername(): String? {
        if (!isInitialized.get()) {
            Logger.e(TAG, "TenjinSDK not initialized. Please call initialize() first.")
            return null
        }
        return userDao.getUsername()?.username
    }

    fun removeUsername() {
        if (!isInitialized.get()) {
            Logger.e(TAG, "TenjinSDK not initialized. Please call initialize() first.")
            return
        }
        coroutineScope.launch {
            userDao.removeUsername()
            Logger.d(TAG, "Username removed.")
        }
    }

    fun sendEvent(eventName: String) {
        if (!isInitialized.get()) {
            Logger.e(TAG, "TenjinSDK not initialized. Please call initialize() first.")
            return
        }
        coroutineScope.launch {
            val currentEventCount = eventDao.getEventsCount()
            if (currentEventCount >= config.eventQueueSizeLimit) {
                Logger.e(TAG, "Event queue size limit reached. Discarding event: $eventName")
                return@launch
            }
            eventDao.insert(Event(eventName = eventName))
            Logger.d(TAG, "Event enqueued: $eventName")
            enqueueBatchEventWork()
        }
    }

    fun getEvents(): Flow<List<Event>>? {
        if (!isInitialized.get()) {
            Logger.e(TAG, "TenjinSDK not initialized. Please call initialize() first.")
            return null
        }
        return eventDao.getAllEvents()
    }
    private fun enqueueBatchEventWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val batchEventWorkRequest = OneTimeWorkRequestBuilder<BatchEventWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            "batchEventWork",
            ExistingWorkPolicy.REPLACE,
            batchEventWorkRequest
        )
    }

    companion object {
        private const val TAG = "TenjinSDK"

        @Volatile
        private var instance: TenjinSDK? = null

        fun getInstance(context: Context, config: TenjinSDKConfig): TenjinSDK {
            return instance ?: synchronized(this) {
                instance ?: build(context, config).also { instance = it }
            }
        }

        private fun build(context: Context, config: TenjinSDKConfig): TenjinSDK {
            val appContext = context.applicationContext
            val database = AppDatabase.getDatabase(appContext)
            val workManager = WorkManager.getInstance(appContext)

            val tenjinSDK = TenjinSDK(
                appContext = appContext,
                config = config,
                eventDao = database.eventDao(),
                userDao = database.userDao(),
                workManager = workManager
            )
            tenjinSDK.initialize()
            return tenjinSDK
        }
    }
}