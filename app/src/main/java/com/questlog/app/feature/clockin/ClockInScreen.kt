@file:OptIn(ExperimentalMaterial3Api::class)

package com.questlog.app.feature.clockin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.questlog.app.R
import com.questlog.app.core.haptic.Haptics
import com.questlog.app.core.model.GameStatus
import com.questlog.app.data.repository.CheckInResult
import com.questlog.app.data.repository.StreakTier
import com.questlog.app.ui.designsystem.ConfettiOverlay
import com.questlog.app.ui.designsystem.QuestlogPrimaryButton
import com.questlog.app.ui.designsystem.QuestlogProgressIndicator
import com.questlog.app.ui.designsystem.QuestlogSage
import com.questlog.app.ui.designsystem.QuestlogSoftBrown
import com.questlog.app.ui.designsystem.QuestlogSpacing
import com.questlog.app.ui.designsystem.QuestlogTerracotta
import com.questlog.app.ui.designsystem.rememberConfettiState
import kotlinx.coroutines.flow.collect

@Composable
fun ClockInScreen(
    onViewStats: () -> Unit,
    viewModel: ClockInViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val confetti = rememberConfettiState()
    val view = LocalView.current

    LaunchedEffect(Unit) {
        viewModel.checkInResult.collect { result ->
            if (result is CheckInResult.CheckedIn) {
                confetti.launch()
                Haptics.success(view)
                viewModel.consumeCheckInResult()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_clock_in)) })
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
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> ClockInContent(
                    uiState = uiState,
                    onCheckIn = viewModel::checkIn,
                    onViewStats = onViewStats,
                )
            }

            ConfettiOverlay(state = confetti)
        }
    }
}

@Composable
private fun ClockInContent(
    uiState: ClockInUiState,
    onCheckIn: () -> Unit,
    onViewStats: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(QuestlogSpacing.L),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(QuestlogSpacing.M))

        StreakFlame(streak = uiState.currentStreak)

        Spacer(modifier = Modifier.height(QuestlogSpacing.M))

        Text(
            text = "${uiState.currentStreak}",
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = pluralStringResource(
                R.plurals.clockin_day_streak,
                uiState.currentStreak,
                uiState.currentStreak,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = QuestlogSoftBrown,
        )

        Spacer(modifier = Modifier.height(QuestlogSpacing.S))

        Text(
            text = if (uiState.tier == StreakTier.FLAMEKEEPER) {
                stringResource(R.string.streak_flamekeeper, uiState.currentStreak)
            } else {
                tierName(uiState.tier)
            },
            style = MaterialTheme.typography.titleMedium,
            color = QuestlogTerracotta,
        )

        Spacer(modifier = Modifier.height(QuestlogSpacing.L))

        TierProgress(currentStreak = uiState.currentStreak)

        Spacer(modifier = Modifier.height(QuestlogSpacing.L))

        QuestlogPrimaryButton(
            onClick = onCheckIn,
            text = stringResource(R.string.action_clock_in),
        )

        Spacer(modifier = Modifier.height(QuestlogSpacing.M))

        Row(horizontalArrangement = Arrangement.spacedBy(QuestlogSpacing.L)) {
            Text(
                text = stringResource(R.string.clockin_longest, uiState.longestStreak),
                style = MaterialTheme.typography.bodySmall,
                color = QuestlogSoftBrown,
            )
            Text(
                text = stringResource(R.string.clockin_total, uiState.totalCheckIns),
                style = MaterialTheme.typography.bodySmall,
                color = QuestlogSoftBrown,
            )
        }

        Spacer(modifier = Modifier.height(QuestlogSpacing.Xl))

        ProgressSection(uiState = uiState)

        Spacer(modifier = Modifier.height(QuestlogSpacing.M))

        Text(
            text = stringResource(R.string.clockin_view_all_stats),
            style = MaterialTheme.typography.labelLarge,
            color = QuestlogTerracotta,
            modifier = Modifier
                .clickable(onClick = onViewStats)
                .padding(QuestlogSpacing.S),
        )

        Spacer(modifier = Modifier.height(QuestlogSpacing.Xl))
    }
}

@Composable
private fun TierProgress(currentStreak: Int) {
    val (tierStart, nextTierStart) = tierBounds(currentStreak)
    val progress = nextTierStart
        ?.let { ((currentStreak - tierStart).toFloat() / (it - tierStart)).coerceIn(0f, 1f) }
        ?: 1f

    Column(modifier = Modifier.fillMaxWidth()) {
        QuestlogProgressIndicator(
            progress = progress,
            color = QuestlogTerracotta,
        )
        if (nextTierStart != null) {
            Spacer(modifier = Modifier.height(QuestlogSpacing.Xs))
            Text(
                text = stringResource(
                    R.string.clockin_next_tier,
                    tierName(StreakTier.forStreak(nextTierStart)),
                ),
                style = MaterialTheme.typography.labelMedium,
                color = QuestlogSoftBrown,
            )
        }
    }
}

@Composable
private fun ProgressSection(uiState: ClockInUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(QuestlogSpacing.L)) {
            Text(
                text = stringResource(R.string.clockin_progress_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(QuestlogSpacing.M))
            GameStatus.entries.forEach { status ->
                val count = uiState.gamesByStatus[status] ?: 0
                val fraction = if (uiState.totalGames > 0) count.toFloat() / uiState.totalGames else 0f
                StatusRow(status = status, count = count, fraction = fraction)
                if (status != GameStatus.entries.last()) {
                    Spacer(modifier = Modifier.height(QuestlogSpacing.S))
                }
            }
            Spacer(modifier = Modifier.height(QuestlogSpacing.M))
            Text(
                text = stringResource(R.string.clockin_verified, uiState.verifiedCompletions),
                style = MaterialTheme.typography.bodyMedium,
                color = QuestlogSage,
            )
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
                text = statusLabel(status),
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
private fun tierName(tier: StreakTier): String = when (tier) {
    StreakTier.NONE -> stringResource(R.string.clockin_tier_none)
    StreakTier.WARM -> stringResource(R.string.clockin_tier_warm)
    StreakTier.FLAMEKEEPER -> stringResource(R.string.clockin_tier_flamekeeper)
    StreakTier.INFERNO -> stringResource(R.string.clockin_tier_inferno)
    StreakTier.LEGEND -> stringResource(R.string.clockin_tier_legend)
}

@Composable
private fun statusLabel(status: GameStatus): String = when (status) {
    GameStatus.WANT -> stringResource(R.string.status_want_to_play)
    GameStatus.PLAYING -> stringResource(R.string.status_playing)
    GameStatus.COMPLETED -> stringResource(R.string.status_completed)
    GameStatus.DROPPED -> stringResource(R.string.status_dropped)
}

private val GameStatus.dotColor: Color
    get() = when (this) {
        GameStatus.WANT -> QuestlogTerracotta
        GameStatus.PLAYING -> QuestlogTerracotta
        GameStatus.COMPLETED -> QuestlogSage
        GameStatus.DROPPED -> QuestlogSoftBrown
    }

private fun tierBounds(streak: Int): Pair<Int, Int?> = when {
    streak >= 30 -> 30 to null
    streak >= 14 -> 14 to 30
    streak >= 7 -> 7 to 14
    streak >= 3 -> 3 to 7
    else -> 0 to 3
}
