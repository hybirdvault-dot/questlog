package com.questlog.app.feature.settings

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.app.core.model.WalletState
import com.questlog.app.core.wallet.MwaWalletClient
import com.questlog.app.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val mwaWalletClient: MwaWalletClient,
) : ViewModel() {

    val walletState: StateFlow<WalletState> = mwaWalletClient.state

    fun connectWallet(activity: ComponentActivity) {
        viewModelScope.launch {
            mwaWalletClient.connect(activity)
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            gameRepository.deleteAll()
        }
    }
}
