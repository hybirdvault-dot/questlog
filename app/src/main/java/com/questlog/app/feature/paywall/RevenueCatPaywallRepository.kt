package com.questlog.app.feature.paywall

import android.app.Activity
import com.questlog.app.data.repository.PaywallRepository
import com.questlog.app.data.repository.PaywallState
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.UpdatedCustomerInfoListener
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith
import com.revenuecat.purchases.restorePurchasesWith
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine

class RevenueCatPaywallRepository @Inject constructor(
    private val purchases: Purchases,
) : PaywallRepository {

    private val _paywallState = MutableStateFlow<PaywallState>(PaywallState.Loading)
    override val paywallState: StateFlow<PaywallState> = _paywallState.asStateFlow()

    init {
        purchases.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
            updateState(customerInfo)
        }

        purchases.getCustomerInfoWith(
            onError = { _paywallState.value = PaywallState.Error(it.message) },
            onSuccess = { updateState(it) },
        )
    }

    private fun updateState(customerInfo: CustomerInfo) {
        val isPro = customerInfo.entitlements["pro"]?.isActive == true
        if (isPro) {
            _paywallState.value = PaywallState.Pro
        } else {
            purchases.getOfferingsWith(
                onError = { _paywallState.value = PaywallState.NotPro(null) },
                onSuccess = { _paywallState.value = PaywallState.NotPro(it.current) },
            )
        }
    }

    override suspend fun purchase(activity: Activity, pkg: Package): Result<Unit> {
        return suspendCancellableCoroutine { cont ->
            purchases.purchaseWith(
                PurchaseParams.Builder(activity, pkg).build(),
                onError = { error, cancelled ->
                    if (!cancelled) {
                        cont.resume(Result.failure(Exception(error.message)))
                    } else {
                        cont.resume(Result.failure(Exception("Purchase cancelled")))
                    }
                },
                onSuccess = { _, _ -> cont.resume(Result.success(Unit)) },
            )
        }
    }

    override suspend fun restorePurchases(): Result<Unit> {
        return suspendCancellableCoroutine { cont ->
            purchases.restorePurchasesWith(
                onError = { cont.resume(Result.failure(Exception(it.message))) },
                onSuccess = { cont.resume(Result.success(Unit)) },
            )
        }
    }
}
