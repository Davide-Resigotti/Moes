package com.moes.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moes.routes.Routes
import com.moes.ui.screens.AuthScreen
import com.moes.ui.screens.HomeScreen
import com.moes.ui.screens.SessionDetailScreen
import com.moes.ui.screens.SessionsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen(Routes.HOME, "Home", Icons.Filled.Home)
    object Sessions : Screen(Routes.SESSIONS, "Sessions", Icons.Filled.History)
    object Account : Screen(Routes.ACCOUNT, "Account", Icons.Filled.AccountCircle)
}

@Composable
fun MoesNavHost() {
    val navController = rememberNavController()

    val items = listOf(
        Screen.Home,
        Screen.Sessions,
        Screen.Account,
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                // COLLEGAMENTO 1: Quando finisce l'allenamento, vai ai dettagli in modalità POST-WORKOUT (true)
                HomeScreen(
                    onNavigateToSummary = { sessionId ->
                        navController.navigate(
                            Routes.sessionDetail(sessionId, isPostWorkout = true)
                        )
                    }
                )
            }
            composable(Routes.ACCOUNT) {
                AuthScreen()
            }
            composable(Routes.SESSIONS) {
                // COLLEGAMENTO 2: Quando clicchi una card, vai ai dettagli in modalità LETTURA (false)
                SessionsScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate(
                            Routes.sessionDetail(sessionId, isPostWorkout = false)
                        )
                    }
                )
            }
            composable(
                route = Routes.SESSION_DETAIL,
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.StringType },
                    navArgument("isPostWorkout") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val sessionId =
                    backStackEntry.arguments?.getString("sessionId") ?: return@composable
                val isPostWorkout = backStackEntry.arguments?.getBoolean("isPostWorkout") ?: false

                SessionDetailScreen(
                    sessionId = sessionId,
                    isPostWorkout = isPostWorkout,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
