@file:OptIn(ExperimentalMaterial3Api::class)

package com.questlog.app.feature.capture

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.questlog.app.R
import com.questlog.app.core.haptic.Haptics
import com.questlog.app.core.model.Game
import com.questlog.app.core.model.GamePreview
import com.questlog.app.ui.designsystem.ConfettiOverlay
import com.questlog.app.ui.designsystem.QuestlogGameImage
import com.questlog.app.ui.designsystem.QuestlogSoftBrown
import com.questlog.app.ui.designsystem.QuestlogPrimaryButton
import com.questlog.app.ui.designsystem.QuestlogSecondaryButton
import com.questlog.app.ui.designsystem.QuestlogSpacing
import com.questlog.app.ui.designsystem.QuestlogTerracotta
import com.questlog.app.ui.designsystem.rememberConfettiState
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun CaptureScreen(
    onGameSaved: () -> Unit,
    onBack: () -> Unit,
    onSearchManually: () -> Unit,
    onShare: (Game) -> Unit,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingShare by viewModel.pendingShare.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current
    val confetti = rememberConfettiState()
    var celebrate by remember { mutableStateOf(false) }

    LaunchedEffect(pendingShare) {
        if (pendingShare != null) {
            viewModel.consumePendingShare(context)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is CaptureUiState.Error) {
            Haptics.warning(view)
        }
    }

    LaunchedEffect(celebrate) {
        if (celebrate) {
            confetti.launch()
            Haptics.success(view)
            delay(750)
            onGameSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.capture_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
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
            when (val state = uiState) {
                CaptureUiState.Idle -> CaptureIdleContent(onSearchManually = onSearchManually)

                CaptureUiState.Loading -> CaptureLoadingContent()

                is CaptureUiState.Candidates -> CandidatesContent(
                    candidates = state.candidates,
                    onCandidateClick = { viewModel.selectCandidate(it) },
                )

                is CaptureUiState.Result -> CaptureResultContent(
                    game = state.game,
                    onSave = {
                        viewModel.saveGame(state.game)
                        celebrate = true
                    },
                    onShare = { onShare(state.game) },
                )

                is CaptureUiState.Error -> CaptureErrorContent(
                    message = state.message,
                    onRetry = { viewModel.reset() },
                    onSearchManually = onSearchManually,
                )
            }

            ConfettiOverlay(state = confetti)
        }
    }
}

@Composable
private fun CaptureIdleContent(onSearchManually: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(QuestlogSpacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .padding(QuestlogSpacing.L),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = null,
                tint = QuestlogTerracotta,
                modifier = Modifier.size(48.dp),
            )
        }

        Spacer(modifier = Modifier.height(QuestlogSpacing.L))

        Text(
            text = stringResource(R.string.capture_idle_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(R.string.capture_idle_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = QuestlogSoftBrown,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = QuestlogSpacing.S),
        )

        Spacer(modifier = Modifier.height(QuestlogSpacing.Xl))

        QuestlogPrimaryButton(
            onClick = onSearchManually,
            text = stringResource(R.string.action_search_manually),
        )
    }
}

@Composable
private fun CaptureLoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            color = QuestlogTerracotta,
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp,
        )
        Text(
            text = stringResource(R.string.ocr_scanning),
            style = MaterialTheme.typography.bodyMedium,
            color = QuestlogSoftBrown,
            modifier = Modifier.padding(top = QuestlogSpacing.L),
        )
    }
}

@Composable
private fun CandidatesContent(
    candidates: List<GamePreview>,
    onCandidateClick: (GamePreview) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = QuestlogSpacing.L),
    ) {
        Text(
            text = stringResource(R.string.capture_candidates_title),
            style = MaterialTheme.typography.labelLarge,
            color = QuestlogTerracotta,
            modifier = Modifier.padding(vertical = QuestlogSpacing.M),
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(QuestlogSpacing.M),
            contentPadding = PaddingValues(bottom = QuestlogSpacing.Xl),
        ) {
            items(items = candidates, key = { it.rawgId }) { candidate ->
                CandidateRow(
                    candidate = candidate,
                    onClick = { onCandidateClick(candidate) },
                )
            }
        }
    }
}

@Composable
private fun CaptureResultContent(
    game: Game,
    onSave: () -> Unit,
    onShare: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(QuestlogSpacing.L),
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
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
                    placeholder = ColorPainter(QuestlogSoftBrown.copy(alpha = 0.1f)),
                    error = ColorPainter(QuestlogSoftBrown.copy(alpha = 0.1f)),
                )

                Column(modifier = Modifier.padding(QuestlogSpacing.L)) {
                    Text(
                        text = stringResource(R.string.capture_match_found),
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
                            text = stringResource(
                                R.string.game_rating,
                                String.format(Locale.US, "%.1f", rating),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = QuestlogSpacing.Xs),
                        )
                    }

                    game.description?.let { description ->
                        Text(
                            text = description.take(200),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = QuestlogSpacing.S),
                        )
                    }

                    Spacer(modifier = Modifier.height(QuestlogSpacing.L))

                    QuestlogPrimaryButton(
                        onClick = onSave,
                        text = stringResource(R.string.action_add_to_library),
                    )
                    Spacer(modifier = Modifier.height(QuestlogSpacing.S))
                    QuestlogSecondaryButton(
                        onClick = onShare,
                        text = stringResource(R.string.action_brag),
                    )
                }
            }
        }
    }
}

@Composable
private fun CaptureErrorContent(
    message: String,
    onRetry: () -> Unit,
    onSearchManually: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(QuestlogSpacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(QuestlogSpacing.L))
        QuestlogPrimaryButton(
            onClick = onRetry,
            text = stringResource(R.string.action_try_again),
        )
        Spacer(modifier = Modifier.height(QuestlogSpacing.S))
        QuestlogSecondaryButton(
            onClick = onSearchManually,
            text = stringResource(R.string.action_search_manually),
        )
    }
}

@Composable
private fun CandidateRow(candidate: GamePreview, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(modifier = Modifier.padding(QuestlogSpacing.M)) {
            QuestlogGameImage(
                url = candidate.coverUrl,
                contentDescription = candidate.title,
                modifier = Modifier
                    .width(56.dp)
                    .aspectRatio(2f / 3f),
            )
            Column(
                modifier = Modifier
                    .padding(start = QuestlogSpacing.M)
                    .align(Alignment.CenterVertically),
            ) {
                Text(
                    text = candidate.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val meta = listOfNotNull(
                    candidate.releaseYear?.toString(),
                    candidate.rawgRating?.let { String.format(Locale.US, "%.1f", it) },
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
