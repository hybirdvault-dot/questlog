package com.questlog.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.app.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val gameRepository: GameRepository,
) : ViewModel() {

    fun deleteAllData() {
        viewModelScope.launch {
            gameRepository.deleteAll()
        }
    }
}
