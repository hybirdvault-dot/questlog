package com.questlog.app.feature.capture

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.app.core.model.Game
import com.questlog.app.core.model.GamePreview
import com.questlog.app.core.network.rawg.RawgRepository
import com.questlog.app.core.ocr.OcrEngine
import com.questlog.app.data.repository.GameRepository
import com.questlog.app.navigation.PendingShare
import com.questlog.app.navigation.ShareEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val rawgRepository: RawgRepository,
    private val gameRepository: GameRepository,
    private val ocrEngine: OcrEngine,
    private val shareEventBus: ShareEventBus,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CaptureUiState>(CaptureUiState.Idle)
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    val pendingShare: StateFlow<PendingShare?> = shareEventBus.pending

    fun consumePendingShare(context: Context) {
        when (val share = shareEventBus.consume()) {
            is PendingShare.Text -> processSharedText(share.body)
            is PendingShare.Image -> processSharedImage(share.uri, context)
            null -> Unit
        }
    }

    fun processSharedText(text: String) {
        viewModelScope.launch {
            _uiState.value = CaptureUiState.Loading
            rawgRepository.search(text.take(100))
                .onSuccess { games ->
                    _uiState.value = if (games.isEmpty()) {
                        CaptureUiState.Error("No games found. Try searching manually.")
                    } else {
                        CaptureUiState.Candidates(games)
                    }
                }
                .onFailure { _uiState.value = CaptureUiState.Error(it.message ?: "Search failed") }
        }
    }

    fun processSharedImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = CaptureUiState.Loading
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    decodeSampled(context, uri, MAX_IMAGE_DIMENSION)
                }
                if (bitmap == null) {
                    _uiState.value = CaptureUiState.Error("Could not load image")
                    return@launch
                }

                val textLines = ocrEngine.extractText(bitmap)
                if (textLines.isEmpty()) {
                    _uiState.value = CaptureUiState.Error("No text found in image")
                    return@launch
                }

                for (line in textLines.take(5)) {
                    val result = rawgRepository.search(line)
                    result.onSuccess { games ->
                        if (games.isNotEmpty()) {
                            _uiState.value = CaptureUiState.Candidates(games)
                            return@launch
                        }
                    }
                }

                _uiState.value = CaptureUiState.Error("No games found. Try searching manually.")
            } catch (e: Exception) {
                _uiState.value = CaptureUiState.Error(e.message ?: "OCR failed")
            }
        }
    }

    fun selectCandidate(game: GamePreview) {
        viewModelScope.launch {
            rawgRepository.getDetail(game.rawgId)
                .onSuccess { _uiState.value = CaptureUiState.Result(it) }
                .onFailure { _uiState.value = CaptureUiState.Error(it.message ?: "Failed to get details") }
        }
    }

    private val _saveSucceeded = MutableStateFlow(false)
    val saveSucceeded: StateFlow<Boolean> = _saveSucceeded.asStateFlow()

    private val _alreadyInLibrary = MutableStateFlow(false)
    val alreadyInLibrary: StateFlow<Boolean> = _alreadyInLibrary.asStateFlow()

    fun saveGame(game: Game) {
        viewModelScope.launch {
            val existing = game.rawgId?.let { gameRepository.findByRawgId(it) }
            if (existing == null) {
                gameRepository.saveGame(game)
                _saveSucceeded.value = true
            } else {
                _alreadyInLibrary.value = true
            }
        }
    }

    fun consumeSaveResult() {
        _saveSucceeded.value = false
        _alreadyInLibrary.value = false
    }

    fun reset() {
        _uiState.value = CaptureUiState.Idle
    }

    // Shared screenshots can be 12 MP+; decoding at full size OOMs on mid-range
    // devices, so sample down to <= 2048 px before OCR.
    private fun decodeSampled(context: Context, uri: Uri, maxDim: Int): android.graphics.Bitmap? {
        val resolver = context.contentResolver
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxDim || bounds.outHeight / (sample * 2) >= maxDim) {
            sample *= 2
        }
        val opts = android.graphics.BitmapFactory.Options().apply { inSampleSize = sample }
        return resolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, opts) }
    }

    private companion object {
        const val MAX_IMAGE_DIMENSION = 2048
    }
}
