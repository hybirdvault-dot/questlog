package com.questlog.app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.questlog.app.data.repository.GameRepository
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun DeepLinkHandler(
    uri: Uri?,
    navController: NavHostController,
    gameRepository: GameRepository,
) {
    LaunchedEffect(uri) {
        if (uri == null) return@LaunchedEffect

        val gameId = uri.lastPathSegment
        if (gameId.isNullOrBlank()) {
            navController.navigateToLibrary()
            return@LaunchedEffect
        }

        val game = gameRepository.observeGame(gameId).firstOrNull()
        if (game != null) {
            navController.navigateToGameDetail(gameId)
        } else {
            navController.navigateToLibrary()
        }
    }
}
