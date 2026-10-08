package com.questlog.app.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.questlog.app.R
import com.questlog.app.feature.capture.CaptureScreen
import com.questlog.app.feature.clockin.ClockInScreen
import com.questlog.app.feature.discover.DiscoverScreen
import com.questlog.app.feature.game.GameDetailScreen
import com.questlog.app.feature.library.LibraryScreen
import com.questlog.app.feature.scan.ScanScreen
import com.questlog.app.feature.settings.SettingsScreen
import com.questlog.app.feature.stats.StatsScreen

private data class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = QuestlogRoute.Library.route,
        labelRes = R.string.nav_library,
        selectedIcon = Icons.Filled.LibraryBooks,
        unselectedIcon = Icons.Outlined.LibraryBooks,
    ),
    BottomNavItem(
        route = QuestlogRoute.Discover.route,
        labelRes = R.string.nav_discover,
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
    ),
    BottomNavItem(
        route = QuestlogRoute.ClockIn.route,
        labelRes = R.string.nav_clock_in,
        selectedIcon = Icons.Filled.LocalFireDepartment,
        unselectedIcon = Icons.Outlined.LocalFireDepartment,
    ),
    BottomNavItem(
        route = QuestlogRoute.Settings.route,
        labelRes = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    ),
)

private val routesWithBottomNav = setOf(
    QuestlogRoute.Library.route,
    QuestlogRoute.Discover.route,
    QuestlogRoute.ClockIn.route,
    QuestlogRoute.Settings.route,
)

private const val ANIM_DURATION = 300

@Composable
fun QuestlogApp(shareEventBus: ShareEventBus) {
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
                                    contentDescription = stringResource(item.labelRes),
                                )
                            },
                            label = { Text(stringResource(item.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        QuestlogNavHost(
            navController = navController,
            shareEventBus = shareEventBus,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun QuestlogNavHost(
    navController: NavHostController,
    shareEventBus: ShareEventBus,
    modifier: Modifier = Modifier,
) {
    val pendingShare by shareEventBus.pending.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(pendingShare, currentRoute) {
        if (pendingShare != null && currentRoute != QuestlogRoute.Capture.route) {
            navController.navigate(QuestlogRoute.Capture.route) {
                launchSingleTop = true
            }
        }
    }

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
                onScanClick = { navController.navigate(QuestlogRoute.Scan.route) },
            )
        }

        composable(QuestlogRoute.Discover.route) {
            DiscoverScreen()
        }

        composable(QuestlogRoute.ClockIn.route) {
            ClockInScreen(
                onViewStats = { navController.navigate(QuestlogRoute.Stats.route) },
            )
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

        composable(QuestlogRoute.Scan.route) {
            ScanScreen(
                onBack = { navController.popBackStack() },
                onConfirmed = {
                    navController.navigate(QuestlogRoute.Capture.route) {
                        popUpTo(QuestlogRoute.Scan.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(
            route = QuestlogRoute.GameDetail.ROUTE,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType }),
        ) {
            GameDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(QuestlogRoute.Stats.route) {
            StatsScreen(
                onShareStats = { /* Share handled in screen */ },
                onBack = { navController.popBackStack() },
            )
        }

        composable(QuestlogRoute.Settings.route) {
            SettingsScreen()
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
