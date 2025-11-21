package com.tenjin.sdk

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit

object TenjinSDK {

    private lateinit var appContext: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var eventDao: EventDao
    private lateinit var userDao: UserDao
    private lateinit var workManager: WorkManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun initialize(context: Context, apiKey: String) {
        appContext = context.applicationContext
        sharedPreferences = appContext.getSharedPreferences("tenjin_sdk_prefs", Context.MODE_PRIVATE)
        val database = AppDatabase.getDatabase(appContext)
        eventDao = database.eventDao()
        userDao = database.userDao()
        workManager = WorkManager.getInstance(appContext)

        sharedPreferences.edit { putString("api_key", apiKey) }
    }

    fun setUsername(username: String) {
        coroutineScope.launch {
            userDao.setUsername(User(username = username))
        }
    }

    suspend fun getUsername(): String? {
        return userDao.getUsername()?.username
    }

    fun removeUsername() {
        coroutineScope.launch {
            userDao.removeUsername()
        }
    }

    fun sendEvent(eventName: String) {
        coroutineScope.launch {
            eventDao.insert(Event(eventName = eventName))
            scheduleEventWork()
        }
    }

    private fun scheduleEventWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val eventWorkRequest = OneTimeWorkRequestBuilder<EventWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueue(eventWorkRequest)
    }
}