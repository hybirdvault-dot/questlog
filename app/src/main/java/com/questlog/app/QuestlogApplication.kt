package com.questlog.app

import android.app.Application
import com.onesignal.OneSignal
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuestlogApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
    }
}
