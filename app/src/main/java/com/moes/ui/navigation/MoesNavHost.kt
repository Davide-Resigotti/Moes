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
                    // MODIFICA QUI: Logica "intelligente" per la selezione
                    val isSelected = if (screen == Screen.Sessions) {
                        // Se siamo sulla tab Sessions, controlliamo se la rotta corrente è
                        // Sessions OPPURE SessionDetail
                        currentDestination?.hierarchy?.any { dest ->
                            dest.route == Routes.SESSIONS ||
                                    dest.route == Routes.SESSION_DETAIL // Usa la costante definita in Routes.kt
                        } == true
                    } else {
                        // Comportamento standard per Home e Account
                        currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    }

                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = isSelected, // Usa la nostra variabile calcolata
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
                HomeScreen(
                    onNavigateToSummary = { sessionId ->
                        // 1. Vai alla lista sessioni (resetta stack)
                        navController.navigate(Routes.SESSIONS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }

                        // 2. Apri il dettaglio
                        navController.navigate(Routes.sessionDetail(sessionId))
                    }
                )
            }

            composable(Routes.ACCOUNT) {
                AuthScreen()
            }

            composable(Routes.SESSIONS) {
                SessionsScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate(Routes.sessionDetail(sessionId))
                    }
                )
            }

            composable(
                route = Routes.SESSION_DETAIL,
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable

                SessionDetailScreen(
                    sessionId = sessionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}