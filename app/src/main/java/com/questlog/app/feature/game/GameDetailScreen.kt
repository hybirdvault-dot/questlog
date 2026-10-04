@file:OptIn(ExperimentalMaterial3Api::class)

package com.questlog.app.feature.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.questlog.app.R
import com.questlog.app.core.model.Game
import com.questlog.app.core.model.GameStatus
import com.questlog.app.ui.designsystem.QuestlogProgressIndicator
import com.questlog.app.ui.designsystem.QuestlogSage
import com.questlog.app.ui.designsystem.QuestlogSoftBrown
import com.questlog.app.ui.designsystem.QuestlogSpacing
import com.questlog.app.ui.designsystem.QuestlogTerracotta
import java.util.Locale

private val statusOptions = listOf(
    GameStatus.WANT to R.string.status_want,
    GameStatus.PLAYING to R.string.status_playing,
    GameStatus.COMPLETED to R.string.status_completed,
    GameStatus.DROPPED to R.string.status_dropped,
)

@Composable
fun GameDetailScreen(
    onBack: () -> Unit,
    onShare: (Game) -> Unit,
    viewModel: GameDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val game = uiState.game
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(game?.title ?: stringResource(R.string.game_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (game != null) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.action_more),
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_share)) },
                                onClick = {
                                    showMenu = false
                                    onShare(game)
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Share, contentDescription = null)
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.action_delete),
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = QuestlogTerracotta)
                }

                uiState.errorMessage != null -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(QuestlogSpacing.L),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                game == null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.game_not_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = QuestlogSoftBrown,
                    )
                }

                else -> GameDetailContent(
                    game = game,
                    onStatusChange = viewModel::updateStatus,
                    onRatingChange = viewModel::updateRating,
                    onNotesChange = viewModel::updateNotes,
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_game_title)) },
            text = { Text(stringResource(R.string.delete_game_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteGame()
                        onBack()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameDetailContent(
    game: Game,
    onStatusChange: (GameStatus) -> Unit,
    onRatingChange: (Int?) -> Unit,
    onNotesChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero image
        Box {
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
                placeholder = ColorPainter(QuestlogSoftBrown.copy(alpha = 0.1f)),
                error = ColorPainter(QuestlogSoftBrown.copy(alpha = 0.1f)),
            )
        }

        Column(modifier = Modifier.padding(QuestlogSpacing.L)) {
            // Title and meta
            Text(
                text = game.title,
                style = MaterialTheme.typography.headlineLarge,
            )

            Row(
                modifier = Modifier.padding(top = QuestlogSpacing.Xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                game.releaseYear?.let { year ->
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = QuestlogSoftBrown,
                    )
                }
                game.rawgRating?.let { rating ->
                    if (game.releaseYear != null) {
                        Text(
                            text = "  ·  ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = QuestlogSoftBrown,
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = QuestlogTerracotta,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = " ${String.format(Locale.US, "%.1f", rating)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = QuestlogSoftBrown,
                    )
                }
            }

            // Platform chips
            val platforms = game.platforms.take(4)
            if (platforms.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = QuestlogSpacing.M),
                    horizontalArrangement = Arrangement.spacedBy(QuestlogSpacing.S),
                ) {
                    platforms.forEach { platform ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                text = platform,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(
                                    horizontal = QuestlogSpacing.S,
                                    vertical = 4.dp,
                                ),
                            )
                        }
                    }
                }
            }

            // Description
            game.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.game_about),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = QuestlogSpacing.L),
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = QuestlogSpacing.Xs),
                    )
                }
            }

            Spacer(modifier = Modifier.height(QuestlogSpacing.L))

            // Status section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(QuestlogSpacing.L)) {
                    Text(
                        text = stringResource(R.string.game_status_section),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(QuestlogSpacing.S))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(QuestlogSpacing.S),
                    ) {
                        statusOptions.forEach { (status, labelRes) ->
                            val isSelected = status == game.status
                            val animatedColor by animateColorAsState(
                                targetValue = if (isSelected) status.color else Color.Transparent,
                                label = "chipColor",
                            )
                            FilterChip(
                                selected = isSelected,
                                onClick = { onStatusChange(status) },
                                label = {
                                    Text(
                                        text = stringResource(labelRes),
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = animatedColor,
                                    selectedLabelColor = Color.White,
                                ),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(QuestlogSpacing.M))

            // Rating section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(QuestlogSpacing.L)) {
                    Text(
                        text = stringResource(R.string.game_your_rating),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(QuestlogSpacing.S))
                    StarRatingRow(
                        rating = game.personalRating,
                        onRatingChange = onRatingChange,
                    )
                }
            }

            Spacer(modifier = Modifier.height(QuestlogSpacing.M))

            // Notes section
            OutlinedTextField(
                value = game.notes.orEmpty(),
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.game_notes_label)) },
                placeholder = { Text(stringResource(R.string.game_notes_placeholder)) },
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(modifier = Modifier.height(QuestlogSpacing.Xl))
        }
    }
}

@Composable
private fun StarRatingRow(rating: Int?, onRatingChange: (Int?) -> Unit) {
    val current = rating ?: 0
    Row(
        horizontalArrangement = Arrangement.spacedBy(QuestlogSpacing.Xs),
    ) {
        repeat(5) { index ->
            val value = index + 1
            Icon(
                imageVector = if (value <= current) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = stringResource(R.string.game_rate_star, value),
                tint = if (value <= current) QuestlogTerracotta else QuestlogSoftBrown.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onRatingChange(if (current == value) null else value) },
            )
        }
    }
}

private val GameStatus.color: Color
    get() = when (this) {
        GameStatus.WANT -> QuestlogTerracotta
        GameStatus.PLAYING -> QuestlogTerracotta
        GameStatus.COMPLETED -> QuestlogSage
        GameStatus.DROPPED -> QuestlogSoftBrown
    }
