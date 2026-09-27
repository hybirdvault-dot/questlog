package com.questlog.app.feature.capture

import android.content.Intent
import android.net.Uri
import javax.inject.Inject

sealed class ShareInput {
    data class Text(val text: String) : ShareInput()
    data class Image(val uri: Uri) : ShareInput()
}

class ShareIntentParser @Inject constructor() {

    fun parse(intent: Intent): ShareInput? {
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                when {
                    intent.hasExtra(Intent.EXTRA_TEXT) -> {
                        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                        if (!text.isNullOrBlank()) ShareInput.Text(text) else null
                    }

                    intent.type?.startsWith("image/") == true -> {
                        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                        uri?.let { ShareInput.Image(it) }
                    }

                    else -> null
                }
            }

            else -> null
        }
    }
}
