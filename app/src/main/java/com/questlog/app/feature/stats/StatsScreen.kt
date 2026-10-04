@file:OptIn(ExperimentalMaterial3Api::class)

package com.questlog.app.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.questlog.app.R
import com.questlog.app.core.model.GameStatus
import com.questlog.app.ui.designsystem.QuestlogEmptyState
import com.questlog.app.ui.designsystem.QuestlogProgressIndicator
import com.questlog.app.ui.designsystem.QuestlogSage
import com.questlog.app.ui.designsystem.QuestlogSoftBrown
import com.questlog.app.ui.designsystem.QuestlogSpacing
import com.questlog.app.ui.designsystem.QuestlogTerracotta
import kotlin.math.roundToInt

@Composable
fun StatsScreen(
    onShareStats: () -> Unit,
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
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
            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = QuestlogTerracotta)
                }

                uiState.errorMessage != null -> CenteredMessage(
                    title = stringResource(R.string.error_generic),
                    subtitle = uiState.errorMessage.orEmpty(),
                )

                uiState.totalGames == 0 -> CenteredMessage(
                    title = stringResource(R.string.stats_empty_title),
                    subtitle = stringResource(R.string.stats_empty_subtitle),
                )

                else -> StatsContent(uiState = uiState)
            }
        }
    }
}

@Composable
private fun StatsContent(uiState: StatsUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(QuestlogSpacing.L),
        verticalArrangement = Arrangement.spacedBy(QuestlogSpacing.M),
    ) {
        // Hero stat
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = QuestlogTerracotta,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(QuestlogSpacing.Xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.height(QuestlogSpacing.M))
                    Text(
                        text = "${uiState.totalGames}",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                    )
                    Text(
                        text = stringResource(R.string.stats_total_games),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }
        }

        // Completion card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(QuestlogSpacing.L)) {
                    Text(
                        text = stringResource(R.string.stats_completion_rate),
                        style = MaterialTheme.typography.labelLarge,
                        color = QuestlogSoftBrown,
                    )
                    Spacer(modifier = Modifier.height(QuestlogSpacing.S))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = "${(uiState.completionRate * 100).roundToInt()}",
                            style = MaterialTheme.typography.displayLarge,
                        )
                        Text(
                            text = stringResource(R.string.stats_percent),
                            style = MaterialTheme.typography.headlineMedium,
                            color = QuestlogSoftBrown,
                            modifier = Modifier.padding(bottom = QuestlogSpacing.S),
                        )
                    }
                    Spacer(modifier = Modifier.height(QuestlogSpacing.S))
                    QuestlogProgressIndicator(
                        progress = uiState.completionRate,
                        color = QuestlogSage,
                    )
                    Spacer(modifier = Modifier.height(QuestlogSpacing.Xs))
                    Text(
                        text = stringResource(
                            R.string.stats_completed_of_total,
                            uiState.completedGames,
                            uiState.totalGames,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = QuestlogSoftBrown,
                    )
                }
            }
        }

        // Status breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(QuestlogSpacing.L)) {
                    Text(
                        text = stringResource(R.string.stats_by_status),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(QuestlogSpacing.M))
                    GameStatus.entries.forEach { status ->
                        val count = uiState.gamesByStatus[status] ?: 0
                        val fraction = if (uiState.totalGames > 0) count.toFloat() / uiState.totalGames else 0f
                        StatusRow(
                            status = status,
                            count = count,
                            fraction = fraction,
                        )
                        if (status != GameStatus.entries.last()) {
                            Spacer(modifier = Modifier.height(QuestlogSpacing.S))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusRow(status: GameStatus, count: Int, fraction: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(status.dotColor),
            )
            Text(
                text = status.label(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = QuestlogSpacing.S),
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        QuestlogProgressIndicator(
            progress = fraction,
            color = status.dotColor,
        )
    }
}

@Composable
private fun CenteredMessage(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        QuestlogEmptyState(title = title, subtitle = subtitle)
    }
}

private val GameStatus.dotColor: Color
    get() = when (this) {
        GameStatus.WANT -> QuestlogTerracotta
        GameStatus.PLAYING -> QuestlogTerracotta
        GameStatus.COMPLETED -> QuestlogSage
        GameStatus.DROPPED -> QuestlogSoftBrown
    }

@Composable
private fun GameStatus.label(): String = when (this) {
    GameStatus.WANT -> stringResource(R.string.status_want_to_play)
    GameStatus.PLAYING -> stringResource(R.string.status_playing)
    GameStatus.COMPLETED -> stringResource(R.string.status_completed)
    GameStatus.DROPPED -> stringResource(R.string.status_dropped)
}
