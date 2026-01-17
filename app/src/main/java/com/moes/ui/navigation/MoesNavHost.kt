package com.moes.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // --- CONFIGURAZIONE NAVBAR ---
    val navBarHeight = 64.dp // Più bassa (era 80dp)
    val navBarBottomMargin = 24.dp
    val navBarHorizontalMargin = 24.dp
    val navBarShape = CircleShape // A PILLOLA (Completamente rotonda)

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. CONTENT
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToSummary = { sessionId ->
                        navController.navigate(Routes.SESSIONS) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        navController.navigate(Routes.sessionDetail(sessionId))
                    }
                )
            }
            composable(Routes.ACCOUNT) { AuthScreen() }
            composable(Routes.SESSIONS) {
                SessionsScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate(Routes.sessionDetail(sessionId))
                    }
                )
            }
            composable(
                route = Routes.SESSION_DETAIL,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
                SessionDetailScreen(
                    sessionId = sessionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // 2. NAVBAR FLUTTUANTE
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = navBarHorizontalMargin,
                    end = navBarHorizontalMargin,
                    bottom = navBarBottomMargin
                )
                .shadow(12.dp, shape = navBarShape)
                .clip(navBarShape)
                .background(MaterialTheme.colorScheme.surface)
                .height(navBarHeight)
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val selectedIndex by remember(currentDestination) {
                derivedStateOf {
                    var index = 0
                    items.forEachIndexed { i, screen ->
                        val isSelected = if (screen == Screen.Sessions) {
                            currentDestination?.hierarchy?.any {
                                it.route == Routes.SESSIONS || it.route == Routes.SESSION_DETAIL
                            } == true
                        } else {
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        }
                        if (isSelected) index = i
                    }
                    index
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val totalWidth = maxWidth
                val itemWidth = totalWidth / items.size

                // ANIMAZIONE SLIDER (BOLLA)
                val indicatorOffset by animateDpAsState(
                    targetValue = itemWidth * selectedIndex,
                    animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.7f),
                    label = "indicatorOffset"
                )

                // LA BOLLA (Sfondo grigio che si muove)
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(itemWidth)
                        .fillMaxHeight()
                        // Padding ridotto perché la bar è più bassa
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape) // Bolla interna a pillola
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    )
                }

                // ICONE + TESTO
                Row(modifier = Modifier.fillMaxSize()) {
                    items.forEachIndexed { index, screen ->
                        val isSelected = index == selectedIndex

                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(300),
                            label = "contentColor"
                        )

                        val interactionSource = remember { MutableInteractionSource() }

                        Column(
                            modifier = Modifier
                                .width(itemWidth)
                                .fillMaxHeight()
                                .clickable(interactionSource = interactionSource, indication = null) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = contentColor,
                                modifier = Modifier.height(24.dp)
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp, // Font leggermente più piccolo per la bar bassa
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = contentColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}