package com.questlog.app.core.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OcrEngine @Inject constructor(
    private val recognizer: TextRecognizer,
) {

    suspend fun extractText(bitmap: Bitmap): List<String> = withContext(Dispatchers.Default) {
        val input = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(input).await()
        result.textBlocks
            .flatMap { block -> block.lines }
            .map { it.text.trim() }
            .filter { it.length >= 2 }
            .filter { it.lowercase() !in COMMON_WORDS }
    }

    companion object {
        private val COMMON_WORDS = setOf(
            "the", "and", "for", "you", "are", "not", "but", "can", "had", "her",
            "was", "one", "our", "out", "day", "get", "has", "him", "play", "game",
            "new", "free", "download", "app", "apps", "more",
        )
    }
}
