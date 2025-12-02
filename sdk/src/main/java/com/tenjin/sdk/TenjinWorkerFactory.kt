package com.tenjin.sdk

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

class TenjinWorkerFactory(
    private val eventDao: EventDao,
    private val userDao: UserDao,
    private val tenjinApi: TenjinApi
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            BatchEventWorker::class.java.name -> {
                BatchEventWorker(appContext, workerParameters, eventDao, userDao, tenjinApi, DefaultAdvertisingIdProvider(appContext))
            }
            else -> null
        }
    }
}