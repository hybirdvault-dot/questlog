package com.questlog.app.data.repository

import android.app.Activity
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Package
import kotlinx.coroutines.flow.StateFlow

sealed class PaywallState {
    object Loading : PaywallState()
    data class NotPro(val offering: Offering?) : PaywallState()
    object Pro : PaywallState()
    data class Error(val message: String) : PaywallState()
}

interface PaywallRepository {
    val paywallState: StateFlow<PaywallState>
    suspend fun purchase(activity: Activity, pkg: Package): Result<Unit>
    suspend fun restorePurchases(): Result<Unit>
}
