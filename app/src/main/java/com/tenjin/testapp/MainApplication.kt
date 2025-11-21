package com.tenjin.testapp

import android.app.Application
import com.tenjin.sdk.TenjinSDK

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TenjinSDK.initialize(this, BuildConfig.TENJIN_API_KEY)
    }
}
