package com.questlog.app.feature.game

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.app.core.model.GameStatus
import com.questlog.app.core.model.ProofStatus
import com.questlog.app.core.model.WalletState
import com.questlog.app.core.share.ShareCardRenderer
import com.questlog.app.core.wallet.MwaWalletClient
import com.questlog.app.core.wallet.SignResult
import com.questlog.app.data.repository.GameRepository
import com.questlog.app.data.repository.ProofOfPlayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

@HiltViewModel
class GameDetailViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val shareCardRenderer: ShareCardRenderer,
    private val mwaWalletClient: MwaWalletClient,
    private val proofOfPlayRepository: ProofOfPlayRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val gameId: String = savedStateHandle["gameId"] ?: ""

    val uiState: StateFlow<GameDetailUiState> = gameRepository.observeGame(gameId)
        .map { game -> GameDetailUiState(isLoading = false, game = game) }
        .catch { e ->
            emit(
                GameDetailUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Could not load this game",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GameDetailUiState(),
        )

    private val _proofState = MutableStateFlow<ProofUiState>(ProofUiState.Idle)
    val proofState: StateFlow<ProofUiState> = _proofState.asStateFlow()

    private val proofMutex = Mutex()

    fun signAndVerify(activity: ComponentActivity) {
        if (!proofMutex.tryLock()) return
        viewModelScope.launch {
            try {
                val game = uiState.value.game ?: return@launch
                val wallet = mwaWalletClient.state.value as? WalletState.Connected
                if (wallet == null) {
                    _proofState.value = ProofUiState.WalletNotConnected
                    return@launch
                }

                gameRepository.updateProof(
                    id = game.id,
                    signature = null,
                    proofAt = null,
                    proofStatus = ProofStatus.PENDING,
                    walletAddress = wallet.pubkey,
                )
                _proofState.value = ProofUiState.Signing

                val unsigned = proofOfPlayRepository.buildUnsignedTransaction(game, wallet.pubkey)
                    .getOrElse {
                        persistFailed(game.id, wallet.pubkey)
                        _proofState.value = ProofUiState.SigningFailed("Couldn't prepare the proof")
                        return@launch
                    }

                when (val sign = mwaWalletClient.signTransaction(activity, unsigned)) {
                    is SignResult.NoWallet -> {
                        _proofState.value = ProofUiState.WalletNotConnected
                    }

                    is SignResult.Rejected -> {
                        persistFailed(game.id, wallet.pubkey)
                        _proofState.value = ProofUiState.SigningFailed("Wallet declined")
                    }

                    is SignResult.Signed -> {
                        _proofState.value = ProofUiState.Verifying
                        proofOfPlayRepository.submitProof(sign.signedTx)
                            .onSuccess { signature ->
                                gameRepository.updateProof(
                                    id = game.id,
                                    signature = signature,
                                    proofAt = Clock.System.now(),
                                    proofStatus = ProofStatus.CONFIRMED,
                                    walletAddress = wallet.pubkey,
                                )
                                _proofState.value = ProofUiState.Verified(signature)
                            }
                            .onFailure {
                                persistFailed(game.id, wallet.pubkey)
                                _proofState.value = ProofUiState.VerificationFailed("Couldn't verify on-chain")
                            }
                    }
                }
            } catch (t: Throwable) {
                uiState.value.game?.let { game ->
                    val walletAddress = (mwaWalletClient.state.value as? WalletState.Connected)?.pubkey
                    persistFailed(game.id, walletAddress)
                }
                _proofState.value = ProofUiState.VerificationFailed(t.message ?: "Verification failed")
            } finally {
                proofMutex.unlock()
            }
        }
    }

    private suspend fun persistFailed(gameId: String, walletAddress: String?) {
        gameRepository.updateProof(
            id = gameId,
            signature = null,
            proofAt = null,
            proofStatus = ProofStatus.FAILED,
            walletAddress = walletAddress,
        )
    }

    fun updateStatus(status: GameStatus) {
        val game = uiState.value.game ?: return
        viewModelScope.launch {
            val updated = game.copy(
                status = status,
                updatedAt = Clock.System.now(),
                completedAt = if (status == GameStatus.COMPLETED) {
                    Clock.System.now()
                } else {
                    game.completedAt
                },
            )
            gameRepository.updateGame(updated)
        }
    }

    fun updateRating(rating: Int?) {
        val game = uiState.value.game ?: return
        viewModelScope.launch {
            gameRepository.updateGame(
                game.copy(personalRating = rating, updatedAt = Clock.System.now()),
            )
        }
    }

    fun updateNotes(notes: String) {
        val game = uiState.value.game ?: return
        viewModelScope.launch {
            gameRepository.updateGame(
                game.copy(notes = notes, updatedAt = Clock.System.now()),
            )
        }
    }

    fun deleteGame() {
        viewModelScope.launch {
            gameRepository.deleteGame(gameId)
        }
    }

    suspend fun shareCard(context: Context): File? {
        val game = uiState.value.game ?: return null
        return withContext(Dispatchers.Default) {
            val bitmap = shareCardRenderer.renderGameCard(
                game = game,
                verifiedBadge = game.proofStatus == ProofStatus.CONFIRMED,
            )
            shareCardRenderer.saveToCache(bitmap)
        }
    }
}
