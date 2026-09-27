package com.questlog.app.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.questlog.app.core.model.Game
import com.questlog.app.core.model.GameStatus
import com.questlog.app.ui.designsystem.QuestlogCoral
import com.questlog.app.ui.designsystem.QuestlogGray
import com.questlog.app.ui.designsystem.QuestlogGreen
import com.questlog.app.ui.designsystem.QuestlogSpacing
import com.questlog.app.ui.designsystem.QuestlogViolet

@Composable
fun GameCard(game: Game, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(game.coverUrl)
                        .crossfade(200)
                        .build(),
                    contentDescription = game.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f),
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(QuestlogGray.copy(alpha = 0.1f)),
                    error = ColorPainter(QuestlogGray.copy(alpha = 0.1f)),
                )

                // Status badge overlaid on image
                Surface(
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 12.dp, bottomEnd = 0.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Text(
                        text = game.status.shortLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(
                            horizontal = QuestlogSpacing.S,
                            vertical = 3.dp,
                        ),
                    )
                }
            }

            Column(modifier = Modifier.padding(QuestlogSpacing.M)) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                val rating = game.personalRating
                if (rating != null) {
                    Row(
                        modifier = Modifier.padding(top = QuestlogSpacing.Xs),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (index < rating) {
                                    QuestlogCoral
                                } else {
                                    QuestlogGray.copy(alpha = 0.2f)
                                },
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                }

                game.releaseYear?.let { year ->
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = QuestlogGray,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

private val GameStatus.shortLabel: String
    get() = when (this) {
        GameStatus.WANT -> "Want"
        GameStatus.PLAYING -> "Playing"
        GameStatus.COMPLETED -> "Done"
        GameStatus.DROPPED -> "Dropped"
    }
