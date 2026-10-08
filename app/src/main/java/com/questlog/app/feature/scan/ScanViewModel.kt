package com.questlog.app.feature.scan

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import com.questlog.app.navigation.PendingShare
import com.questlog.app.navigation.ShareEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val recognizer: TextRecognizer,
    private val shareEventBus: ShareEventBus,
) : ViewModel() {

    private val _candidate = MutableStateFlow<String?>(null)
    val candidate: StateFlow<String?> = _candidate.asStateFlow()

    private var debounceJob: Job? = null

    fun createAnalyzer(): ScanTextAnalyzer = ScanTextAnalyzer(recognizer) { lines ->
        onOcrLines(lines)
    }

    /**
     * Confirmed title enters the existing capture pipeline (processSharedText).
     * Nothing is saved here — the user still confirms each step (Law 4).
     */
    fun confirm() {
        val title = _candidate.value ?: return
        shareEventBus.publish(PendingShare.Text(title))
        clear()
    }

    internal fun onOcrLines(lines: List<String>) {
        val best = lines
            .map { it.trim() }
            .filter { it.length in 2..MAX_TITLE_LENGTH }
            .filter { text -> text.any { it.isLetter() } }
            .filter { it.lowercase() !in IGNORED_WORDS }
            .maxByOrNull { it.length }
            ?: return
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(CANDIDATE_DEBOUNCE_MS)
            _candidate.value = best
        }
    }

    private fun clear() {
        debounceJob?.cancel()
        _candidate.value = null
    }

    private companion object {
        const val CANDIDATE_DEBOUNCE_MS = 250L
        const val MAX_TITLE_LENGTH = 60

        val IGNORED_WORDS = setOf(
            "the", "and", "for", "you", "are", "not", "but", "can", "had", "her",
            "was", "one", "our", "out", "day", "get", "has", "him", "play", "game",
            "new", "free", "download", "app", "apps", "more",
        )
    }
}

/**
 * Single-flight OCR analyzer: frames are dropped (not queued) while a
 * recognition is already running, on top of KEEP_ONLY_LATEST backpressure.
 */
class ScanTextAnalyzer(
    private val recognizer: TextRecognizer,
    private val onLines: (List<String>) -> Unit,
) : ImageAnalysis.Analyzer {

    private val inFlight = AtomicBoolean(false)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || !inFlight.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(input)
            .addOnSuccessListener { result ->
                onLines(result.textBlocks.flatMap { block -> block.lines }.map { it.text })
            }
            .addOnCompleteListener {
                inFlight.set(false)
                imageProxy.close()
            }
    }
}
