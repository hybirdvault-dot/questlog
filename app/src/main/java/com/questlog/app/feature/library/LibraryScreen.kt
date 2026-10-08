@file:OptIn(ExperimentalMaterial3Api::class)

package com.questlog.app.feature.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.questlog.app.R
import com.questlog.app.core.model.GameStatus
import com.questlog.app.ui.designsystem.QuestlogEmptyState
import com.questlog.app.ui.designsystem.QuestlogSoftBrown
import com.questlog.app.ui.designsystem.QuestlogSpacing
import com.questlog.app.ui.designsystem.QuestlogTerracotta

private val libraryFilters = listOf(
    null to R.string.filter_all,
    GameStatus.PLAYING to R.string.status_playing,
    GameStatus.WANT to R.string.status_want,
    GameStatus.COMPLETED to R.string.status_completed_short,
    GameStatus.DROPPED to R.string.status_dropped,
)

@Composable
fun LibraryScreen(
    onGameClick: (String) -> Unit,
    onCaptureClick: () -> Unit,
    onScanClick: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                actions = {
                    Button(
                        onClick = onScanClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = QuestlogTerracotta,
                            contentColor = Color.White,
                        ),
                        modifier = Modifier.padding(end = QuestlogSpacing.M),
                    ) {
                        Text(
                            text = stringResource(R.string.action_scan_game),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCaptureClick,
                containerColor = QuestlogTerracotta,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.action_add_game),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = QuestlogSpacing.L, vertical = QuestlogSpacing.S),
                horizontalArrangement = Arrangement.spacedBy(QuestlogSpacing.S),
            ) {
                libraryFilters.forEachIndexed { _, (status, labelRes) ->
                    val isSelected = status == uiState.selectedStatus
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectStatus(status) },
                        label = {
                            Text(
                                text = stringResource(labelRes),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = QuestlogTerracotta,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }

            when {
                uiState.errorMessage != null -> CenteredMessage(
                    title = stringResource(R.string.error_generic),
                    subtitle = uiState.errorMessage!!,
                )

                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = QuestlogTerracotta)
                }

                uiState.games.isEmpty() -> CenteredMessage(
                    title = stringResource(R.string.empty_library_title),
                    actionText = stringResource(R.string.action_scan_game),
                    onAction = onScanClick,
                )

                else -> {
                    // Game count
                    Text(
                        text = pluralStringResource(
                            R.plurals.library_game_count,
                            uiState.games.size,
                            uiState.games.size,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = QuestlogSoftBrown,
                        modifier = Modifier.padding(horizontal = QuestlogSpacing.L, vertical = QuestlogSpacing.Xs),
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = QuestlogSpacing.L,
                            end = QuestlogSpacing.L,
                            bottom = 88.dp, // FAB clearance
                        ),
                        horizontalArrangement = Arrangement.spacedBy(QuestlogSpacing.M),
                        verticalArrangement = Arrangement.spacedBy(QuestlogSpacing.M),
                    ) {
                        items(items = uiState.games, key = { it.id }) { game ->
                            GameCard(
                                game = game,
                                onClick = { onGameClick(game.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredMessage(
    title: String,
    subtitle: String = "",
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
