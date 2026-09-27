package com.questlog.app.feature.paywall

import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Package

data class PaywallUiState(
    val isLoading: Boolean = true,
    val isPro: Boolean = false,
    val offering: Offering? = null,
    val purchaseInProgress: Boolean = false,
    val errorMessage: String? = null,
)
