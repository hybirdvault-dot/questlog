package com.questlog.app.feature.game

sealed class ProofUiState {
    object Idle : ProofUiState()
    object WalletNotConnected : ProofUiState()
    data class Signing(val unsignedTransaction: ByteArray) : ProofUiState()
    object Verifying : ProofUiState()
    data class Verified(val signature: String) : ProofUiState()
    object Failed : ProofUiState()
}
