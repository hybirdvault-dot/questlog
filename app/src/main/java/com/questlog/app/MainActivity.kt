package com.questlog.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.questlog.app.navigation.QuestlogApp
import com.questlog.app.ui.designsystem.QuestlogTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            QuestlogTheme {
                QuestlogApp()
            }
        }

        handleShareIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return

        when {
            intent.type?.startsWith("text/") == true -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!text.isNullOrBlank()) {
                    Log.d(TAG, "Shared text: $text")
                }
            }

            intent.type?.startsWith("image/") == true -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                if (uri != null) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    Log.d(TAG, "Shared image: $uri")
                }
            }
        }
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}
