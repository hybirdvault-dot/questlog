package com.questlog.app.feature.game

sealed class ProofUiState {
    data object Idle : ProofUiState()
    data object WalletNotConnected : ProofUiState()
    data object Signing : ProofUiState()
    data object Verifying : ProofUiState()
    data class Verified(val signature: String) : ProofUiState()
    data class SigningFailed(val reason: String) : ProofUiState()
    data class VerificationFailed(val reason: String) : ProofUiState()
}
