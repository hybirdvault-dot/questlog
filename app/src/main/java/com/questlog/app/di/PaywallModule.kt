package com.questlog.app.di

import com.questlog.app.data.repository.PaywallRepository
import com.questlog.app.feature.paywall.RevenueCatPaywallRepository
import com.revenuecat.purchases.Purchases
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PurchasesModule {

    @Provides
    @Singleton
    fun providePurchases(): Purchases = Purchases.sharedInstance
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PaywallModule {

    @Binds
    @Singleton
    abstract fun bindPaywallRepository(
        impl: RevenueCatPaywallRepository,
    ): PaywallRepository
}
