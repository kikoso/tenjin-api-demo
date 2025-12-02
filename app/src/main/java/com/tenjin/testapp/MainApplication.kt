package com.tenjin.testapp

import android.app.Application
import androidx.work.Configuration
import com.tenjin.sdk.AppDatabase
import com.tenjin.sdk.RetrofitClient
import com.tenjin.sdk.TenjinSDK
import com.tenjin.sdk.TenjinSDKConfig
import com.tenjin.sdk.TenjinWorkerFactory

class MainApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() {
            val database = AppDatabase.getDatabase(this)
            val tenjinApi = RetrofitClient.instance
            val workerFactory = TenjinWorkerFactory(database.eventDao(), database.userDao(), tenjinApi)
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
        }

    override fun onCreate() {
        super.onCreate()
        val sdkConfig = TenjinSDKConfig(
            apiKey = BuildConfig.TENJIN_API_KEY,
            isLoggingEnabled = true
        )
        sdk = TenjinSDK.getInstance(this, sdkConfig)
    }

    companion object {
        lateinit var sdk: TenjinSDK
    }
}
