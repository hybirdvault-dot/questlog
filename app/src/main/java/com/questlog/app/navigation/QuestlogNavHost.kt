package com.questlog.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.questlog.app.feature.capture.CaptureScreen
import com.questlog.app.feature.discover.DiscoverScreen
import com.questlog.app.feature.game.GameDetailScreen
import com.questlog.app.feature.library.LibraryScreen
import com.questlog.app.feature.paywall.PaywallScreen
import com.questlog.app.feature.settings.SettingsScreen
import com.questlog.app.feature.stats.StatsScreen

private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = QuestlogRoute.Library.route,
        label = "Library",
        selectedIcon = Icons.Filled.LibraryBooks,
        unselectedIcon = Icons.Outlined.LibraryBooks,
    ),
    BottomNavItem(
        route = QuestlogRoute.Discover.route,
        label = "Discover",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
    ),
    BottomNavItem(
        route = QuestlogRoute.Stats.route,
        label = "Stats",
        selectedIcon = Icons.Filled.PieChart,
        unselectedIcon = Icons.Outlined.PieChart,
    ),
)

private val routesWithBottomNav = setOf(
    QuestlogRoute.Library.route,
    QuestlogRoute.Discover.route,
    QuestlogRoute.Stats.route,
)

private const val ANIM_DURATION = 300

@Composable
fun QuestlogApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in routesWithBottomNav

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(QuestlogRoute.Library.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        QuestlogNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun QuestlogNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = QuestlogRoute.Library.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(ANIM_DURATION)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ANIM_DURATION))
        },
        exitTransition = { fadeOut(animationSpec = tween(ANIM_DURATION)) },
        popEnterTransition = {
            fadeIn(animationSpec = tween(ANIM_DURATION)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ANIM_DURATION))
        },
        popExitTransition = { fadeOut(animationSpec = tween(ANIM_DURATION)) },
    ) {
        composable(QuestlogRoute.Library.route) {
            LibraryScreen(
                onGameClick = { gameId -> navController.navigate("game/$gameId") },
                onCaptureClick = { navController.navigate(QuestlogRoute.Capture.route) },
                onSettingsClick = { navController.navigate(QuestlogRoute.Settings.route) },
            )
        }

        composable(QuestlogRoute.Discover.route) {
            DiscoverScreen()
        }

        composable(QuestlogRoute.Capture.route) {
            CaptureScreen(
                onGameSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                onSearchManually = {
                    navController.popBackStack()
                    navController.navigate(QuestlogRoute.Discover.route)
                },
                onShare = { game ->
                    // Share intent handled within the screen
                },
            )
        }

        composable(
            route = QuestlogRoute.GameDetail.ROUTE,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType }),
        ) {
            GameDetailScreen(
                onBack = { navController.popBackStack() },
                onShare = { /* Share handled in screen */ },
            )
        }

        composable(QuestlogRoute.Stats.route) {
            StatsScreen(
                onShareStats = { /* Share handled in screen */ },
                onBack = { navController.popBackStack() },
            )
        }

        composable(QuestlogRoute.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate(QuestlogRoute.Paywall.route) },
            )
        }

        composable(QuestlogRoute.Paywall.route) {
            PaywallScreen(
                onDismiss = { navController.popBackStack() },
            )
        }
    }
}

// Navigation extension functions
fun NavController.navigateToLibrary() {
    navigate(QuestlogRoute.Library.route) { popUpTo(0) }
}

fun NavController.navigateToGameDetail(gameId: String) {
    navigate("game/$gameId")
}
