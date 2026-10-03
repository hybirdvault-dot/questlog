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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.questlog.app.core.model.GameStatus
import com.questlog.app.ui.designsystem.QuestlogCoral
import com.questlog.app.ui.designsystem.QuestlogEmptyState
import com.questlog.app.ui.designsystem.QuestlogGray
import com.questlog.app.ui.designsystem.QuestlogGreen
import com.questlog.app.ui.designsystem.QuestlogProgressIndicator
import com.questlog.app.ui.designsystem.QuestlogSpacing
import com.questlog.app.ui.designsystem.QuestlogViolet
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
                title = { Text("Your Gaming Story") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                    CircularProgressIndicator(color = QuestlogViolet)
                }

                uiState.errorMessage != null -> CenteredMessage(
                    title = "Something went wrong",
                    subtitle = uiState.errorMessage.orEmpty(),
                )

                uiState.totalGames == 0 -> CenteredMessage(
                    title = "Your gaming story starts here",
                    subtitle = "Save some games and mark them complete.",
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
                    containerColor = QuestlogViolet,
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
                        text = "games in your collection",
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
                        text = "Completion Rate",
                        style = MaterialTheme.typography.labelLarge,
                        color = QuestlogGray,
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
                            text = "%",
                            style = MaterialTheme.typography.headlineMedium,
                            color = QuestlogGray,
                            modifier = Modifier.padding(bottom = QuestlogSpacing.S),
                        )
                    }
                    Spacer(modifier = Modifier.height(QuestlogSpacing.S))
                    QuestlogProgressIndicator(
                        progress = uiState.completionRate,
                        color = QuestlogGreen,
                    )
                    Spacer(modifier = Modifier.height(QuestlogSpacing.Xs))
                    Text(
                        text = "${uiState.completedGames} of ${uiState.totalGames} completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = QuestlogGray,
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
                        text = "By Status",
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
                text = status.label,
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
        GameStatus.WANT -> QuestlogViolet
        GameStatus.PLAYING -> QuestlogCoral
        GameStatus.COMPLETED -> QuestlogGreen
        GameStatus.DROPPED -> QuestlogGray
    }

private val GameStatus.label: String
    get() = when (this) {
        GameStatus.WANT -> "Want to Play"
        GameStatus.PLAYING -> "Playing"
        GameStatus.COMPLETED -> "Completed"
        GameStatus.DROPPED -> "Dropped"
    }
