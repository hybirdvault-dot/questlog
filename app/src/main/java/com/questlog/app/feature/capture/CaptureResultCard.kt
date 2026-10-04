package com.questlog.app.feature.capture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.questlog.app.core.model.Game
import com.questlog.app.ui.designsystem.QuestlogSoftBrown
import com.questlog.app.ui.designsystem.QuestlogPrimaryButton
import com.questlog.app.ui.designsystem.QuestlogSecondaryButton
import com.questlog.app.ui.designsystem.QuestlogSpacing
import com.questlog.app.ui.designsystem.QuestlogTerracotta
import java.util.Locale

@Composable
fun CaptureResultCard(
    game: Game,
    onSave: () -> Unit,
    onShare: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(game.coverUrl)
                    .crossfade(200)
                    .build(),
                contentDescription = game.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(QuestlogSoftBrown.copy(alpha = 0.2f)),
                error = ColorPainter(QuestlogSoftBrown.copy(alpha = 0.2f)),
            )

            Column(modifier = Modifier.padding(QuestlogSpacing.L)) {
                Text(
                    text = "We found a possible match",
                    style = MaterialTheme.typography.labelMedium,
                    color = QuestlogTerracotta,
                )

                Text(
                    text = game.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = QuestlogSpacing.Xs),
                )

                val meta = listOfNotNull(
                    game.releaseYear?.toString(),
                    game.platforms.take(2).joinToString(", ").takeIf { it.isNotEmpty() },
                ).joinToString(" · ")
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = QuestlogSoftBrown,
                        modifier = Modifier.padding(top = QuestlogSpacing.Xs),
                    )
                }

                game.rawgRating?.let { rating ->
                    Text(
                        text = "RAWG: ${String.format(Locale.US, "%.1f", rating)}/5",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = QuestlogSpacing.Xs),
                    )
                }

                game.description?.let { description ->
                    Text(
                        text = if (description.length > 150) {
                            "${description.take(150)}..."
                        } else {
                            description
                        },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = QuestlogSpacing.S),
                    )
                }

                Spacer(modifier = Modifier.height(QuestlogSpacing.L))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuestlogPrimaryButton(onClick = onSave, text = "Save to Library")
                    Spacer(modifier = Modifier.padding(horizontal = QuestlogSpacing.Xs))
                    QuestlogSecondaryButton(onClick = onShare, text = "Share Card")
                }
            }
        }
    }
}
