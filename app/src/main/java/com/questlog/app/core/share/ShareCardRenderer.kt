package com.questlog.app.core.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.questlog.app.R
import com.questlog.app.core.model.Game
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class ShareCardRenderer @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun renderGameCard(game: Game): Bitmap {
        val composeView = ComposeView(context).apply {
            setContent {
                val style = ShareCardStyle.GameCard
                val density = LocalDensity.current
                val widthDp = with(density) { style.widthPx.toDp() }
                val heightDp = with(density) { style.heightPx.toDp() }
                Box(
                    modifier = Modifier
                        .size(widthDp, heightDp)
                        .background(style.background)
                        .padding(48.dp),
                ) {
                    Column {
                        if (game.coverUrl != null) {
                            AsyncImage(
                                model = game.coverUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = game.title,
                            color = style.textColor,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${game.releaseYear} · ${game.platforms.take(2).joinToString(", ")}",
                            color = style.textColor.copy(alpha = 0.7f),
                            fontSize = 20.sp,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                R.string.game_rating,
                                game.rawgRating?.toString() ?: stringResource(R.string.value_unavailable),
                            ),
                            color = style.accentColor,
                            fontSize = 20.sp,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = stringResource(R.string.app_name),
                            color = style.textColor.copy(alpha = 0.4f),
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.End),
                        )
                    }
                }
            }
        }

        val width = 1080
        val height = 1080
        composeView.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
        )
        composeView.layout(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        composeView.draw(Canvas(bitmap))
        return bitmap
    }

    fun saveToCache(bitmap: Bitmap): File {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "questlog_${System.currentTimeMillis()}.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }

    fun createShareIntent(file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
