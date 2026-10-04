package com.questlog.app.core.model

sealed class WalletState {
    object NotConnected : WalletState()
    data class Connected(val pubkey: String) : WalletState()
}
