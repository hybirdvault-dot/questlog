@file:OptIn(ExperimentalMaterial3Api::class)

package com.questlog.app.feature.discover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.questlog.app.core.model.GamePreview
import com.questlog.app.ui.designsystem.QuestlogEmptyState
import com.questlog.app.ui.designsystem.QuestlogGameImage
import com.questlog.app.ui.designsystem.QuestlogSoftBrown
import com.questlog.app.ui.designsystem.QuestlogPrimaryButton
import com.questlog.app.ui.designsystem.QuestlogSecondaryButton
import com.questlog.app.ui.designsystem.QuestlogSpacing
import java.util.Locale

@Composable
fun DiscoverScreen() {
    val viewModel: DiscoverViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val addedToLibrary by viewModel.addedToLibrary.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(addedToLibrary) {
        if (addedToLibrary) {
            snackbarHostState.showSnackbar("Added to your library")
            viewModel.resetAddedFlag()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Discover") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = QuestlogSpacing.L),
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        viewModel.search(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = QuestlogSpacing.M),
                    placeholder = { Text("Search games") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                when (val state = uiState) {
                    DiscoverUiState.Idle -> CenteredMessage(
                        title = "Find your next game",
                        subtitle = "Search by title to add it to your library.",
                    )

                    DiscoverUiState.Loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }

                    is DiscoverUiState.Success -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(QuestlogSpacing.M),
                        contentPadding = PaddingValues(bottom = QuestlogSpacing.Xl),
                    ) {
                        items(items = state.games, key = { it.rawgId }) { game ->
                            DiscoverResultCard(
                                game = game,
                                onClick = { viewModel.fetchDetail(game) },
                            )
                        }
                    }

                    DiscoverUiState.Empty -> CenteredMessage(
                        title = "No games found",
                        subtitle = "Try a different search term.",
                    )

                    is DiscoverUiState.Error -> CenteredMessage(
                        title = "Search failed",
                        subtitle = state.message,
                        actionText = "Try again",
                        onAction = { viewModel.search(query) },
                    )
                }
            }

            // Detail overlay
            AnimatedVisibility(
                visible = detailState != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.fillMaxSize(),
            ) {
                detailState?.let { detail ->
                    GameDetailOverlay(
                        detail = detail,
                        onDismiss = { viewModel.clearDetail() },
                        onAddToLibrary = { viewModel.addToLibrary(detail) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CenteredMessage(
    title: String,
    subtitle: String,
    actionText: String = "",
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (onAction != null) {
            QuestlogEmptyState(
                title = title,
                subtitle = subtitle,
                actionText = actionText,
                action = onAction,
            )
        } else {
            QuestlogEmptyState(title = title, subtitle = subtitle)
        }
    }
}

@Composable
private fun DiscoverResultCard(game: GamePreview, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Row(modifier = Modifier.padding(QuestlogSpacing.M)) {
            QuestlogGameImage(
                url = game.coverUrl,
                contentDescription = game.title,
                modifier = Modifier
                    .width(72.dp)
                    .aspectRatio(2f / 3f),
            )
            Column(
                modifier = Modifier
                    .padding(start = QuestlogSpacing.M)
                    .align(Alignment.CenterVertically),
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val meta = listOfNotNull(
                    game.releaseYear?.toString(),
                    game.rawgRating?.let { String.format(Locale.US, "%.1f", it) },
                ).joinToString(" · ")
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = QuestlogSoftBrown,
                        modifier = Modifier.padding(top = QuestlogSpacing.Xs),
                    )
                }
            }
        }
    }
}

@Composable
private fun GameDetailOverlay(
    detail: com.questlog.app.core.model.Game,
    onDismiss: () -> Unit,
    onAddToLibrary: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(QuestlogSpacing.L),
        ) {
            QuestlogGameImage(
                url = detail.coverUrl,
                contentDescription = detail.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )

            Spacer(modifier = Modifier.height(QuestlogSpacing.L))

            Text(
                text = detail.title,
                style = MaterialTheme.typography.headlineLarge,
            )

            val meta = listOfNotNull(
                detail.releaseYear?.toString(),
                detail.platforms.take(3).joinToString(", ").takeIf { it.isNotEmpty() },
            ).joinToString(" · ")
            if (meta.isNotEmpty()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = QuestlogSoftBrown,
                    modifier = Modifier.padding(top = QuestlogSpacing.Xs),
                )
            }

            detail.rawgRating?.let { rating ->
                Text(
                    text = "RAWG: ${String.format(Locale.US, "%.1f", rating)}/5",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = QuestlogSpacing.S),
                )
            }

            detail.description?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = QuestlogSpacing.M),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(QuestlogSpacing.M),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    QuestlogSecondaryButton(onClick = onDismiss, text = "Cancel")
                }
                Box(modifier = Modifier.weight(1f)) {
                    QuestlogPrimaryButton(onClick = onAddToLibrary, text = "Add to Library")
                }
            }
        }
    }
}
