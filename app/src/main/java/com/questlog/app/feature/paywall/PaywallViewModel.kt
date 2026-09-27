package com.questlog.app.feature.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.app.data.repository.PaywallRepository
import com.questlog.app.data.repository.PaywallState
import com.revenuecat.purchases.Package
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val paywallRepository: PaywallRepository,
) : ViewModel() {

    val uiState: StateFlow<PaywallUiState> = paywallRepository.paywallState
        .map { state ->
            when (state) {
                is PaywallState.Loading -> PaywallUiState(isLoading = true)

                is PaywallState.Pro -> PaywallUiState(
                    isLoading = false,
                    isPro = true,
                )

                is PaywallState.NotPro -> PaywallUiState(
                    isLoading = false,
                    offering = state.offering,
                )

                is PaywallState.Error -> PaywallUiState(
                    isLoading = false,
                    errorMessage = state.message,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PaywallUiState(),
        )

    fun purchase(activity: Activity, pkg: Package) {
        viewModelScope.launch {
            paywallRepository.purchase(activity, pkg)
                .onFailure { /* error handled by paywallState flow */ }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            paywallRepository.restorePurchases()
        }
    }
}
