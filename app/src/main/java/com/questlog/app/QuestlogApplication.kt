package com.questlog.app

import android.app.Application
import com.onesignal.OneSignal
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesAreCompletedBy
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuestlogApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT_API_KEY)
                .purchasesAreCompletedBy(PurchasesAreCompletedBy.REVENUECAT)
                .appUserID(null)
                .diagnosticsEnabled(true)
                .build(),
        )

        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
    }
}
