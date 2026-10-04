package com.questlog.app.core.wallet

import androidx.activity.ComponentActivity
import com.funkatronics.encoders.Base58
import com.questlog.app.core.model.WalletState
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solana.mobilewalletadapter.clientlib.successPayload
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class MwaWalletClient @Inject constructor(
    private val walletAdapter: MobileWalletAdapter,
) {

    private val _state = MutableStateFlow<WalletState>(WalletState.NotConnected)
    val state: StateFlow<WalletState> = _state.asStateFlow()

    val isConnected: Boolean
        get() = _state.value is WalletState.Connected

    suspend fun connect(activity: ComponentActivity): String? {
        val sender = ActivityResultSender(activity)
        return when (val result = walletAdapter.connect(sender)) {
            is TransactionResult.Success<*> -> {
                val authResult = result.authResult
                val account = authResult.accounts?.firstOrNull() ?: return null
                val pubkey = Base58.encodeToString(account.publicKey)
                walletAdapter.authToken = authResult.authToken
                _state.value = WalletState.Connected(pubkey)
                pubkey
            }

            is TransactionResult.NoWalletFound<*> -> null
            is TransactionResult.Failure<*> -> null
        }
    }

    suspend fun signTransaction(activity: ComponentActivity, transaction: ByteArray): ByteArray? {
        val sender = ActivityResultSender(activity)
        val result = walletAdapter.transact(sender) {
            signTransactions(arrayOf(transaction))
        }
        val signed = result.successPayload?.signedPayloads?.firstOrNull()
        return when (result) {
            is TransactionResult.Success<*> -> signed
            is TransactionResult.NoWalletFound<*> -> null
            is TransactionResult.Failure<*> -> null
        }
    }
}
