package com.questlog.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.questlog.app.navigation.PendingShare
import com.questlog.app.navigation.QuestlogApp
import com.questlog.app.navigation.ShareEventBus
import com.questlog.app.ui.designsystem.QuestlogTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var shareEventBus: ShareEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            QuestlogTheme {
                QuestlogApp(shareEventBus = shareEventBus)
            }
        }

        if (savedInstanceState == null) {
            handleShareIntent(intent)
        }
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
                    shareEventBus.publish(PendingShare.Text(text))
                }
            }

            intent.type?.startsWith("image/") == true -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                if (uri != null) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    shareEventBus.publish(PendingShare.Image(uri))
                }
            }
        }
    }
}
