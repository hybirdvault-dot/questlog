package com.questlog.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.app.data.repository.GameRepository
import com.questlog.app.data.repository.PaywallRepository
import com.questlog.app.data.repository.PaywallState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val paywallRepository: PaywallRepository,
    private val gameRepository: GameRepository,
) : ViewModel() {

    val isPro: StateFlow<Boolean> = paywallRepository.paywallState
        .map { it is PaywallState.Pro }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    fun deleteAllData() {
        viewModelScope.launch {
            gameRepository.deleteAll()
        }
    }
}
