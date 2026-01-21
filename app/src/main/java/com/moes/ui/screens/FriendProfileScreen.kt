package com.moes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moes.repositories.GamificationRepository
import com.moes.ui.LocalNetworkStatus
import com.moes.ui.composables.profile.MissionCard
import com.moes.ui.theme.LogoGradientEnd
import com.moes.ui.theme.LogoGradientStart
import com.moes.ui.viewmodels.SocialViewModel
import com.moes.ui.viewmodels.ViewModelFactory
import com.moes.utils.FormatUtils
import com.moes.utils.StatisticsUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendProfileScreen(
    friendId: String,
    onNavigateBack: () -> Unit,
    viewModel: SocialViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val socialUiState by viewModel.uiState.collectAsState()

    val friendInfo = remember(socialUiState.friends, friendId) {
        socialUiState.friends.find { it.userId == friendId }
    }

    val friendStats by viewModel.getFriendStats(friendId).collectAsState(initial = null)

    val friendMissions = remember(friendStats) {
        if (friendStats != null) {
            GamificationRepository.calculateMissions(friendStats!!)
        } else {
            emptyList()
        }
    }

    val currentStreak = friendStats?.currentStreakDays ?: 0
    val lastTrainingDate = friendStats?.lastTrainingDate ?: 0L

    val (showStreak, streakEmoji) = remember(currentStreak, lastTrainingDate) {
        StatisticsUtils.getStreakStatus(currentStreak, lastTrainingDate)
    }

    val isOnline = LocalNetworkStatus.current

    val displayName = friendInfo?.displayName ?: "Utente"
    val initials = if (displayName.isNotEmpty()) displayName.take(1).uppercase() else "?"
    val sinceDate = friendInfo?.since ?: 0L

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        when (friendInfo) {
            null if socialUiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Utente non trovato o rimosso dagli amici.")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // HEADER
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.BottomCenter) {
                                // Avatar Originale
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(LogoGradientStart, LogoGradientEnd)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontWeight = FontWeight.Bold, color = Color.White
                                        )
                                    )
                                }

                                // Badge Streak
                                if (showStreak) {
                                    Box(
                                        modifier = Modifier
                                            .offset(y = 10.dp)
                                            .shadow(4.dp, CircleShape)
                                            .border(
                                                3.dp,
                                                MaterialTheme.colorScheme.background,
                                                CircleShape
                                            )
                                            .clip(CircleShape)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        LogoGradientStart,
                                                        LogoGradientEnd
                                                    )
                                                )
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = streakEmoji, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "$currentStreak",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                ),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            if (sinceDate > 0) {
                                Text(
                                    text = "Amici dal ${FormatUtils.formatDate(sinceDate)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // MISSIONI
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Missioni e Traguardi",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 24.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (friendStats == null) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isOnline) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    Text(
                                        "Statistiche non disponibili offline.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else if (friendMissions.isEmpty()) {
                        item {
                            Text(
                                text = "Nessuna missione completata.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(friendMissions) { mission ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                MissionCard(progress = mission)
                            }
                        }
                    }
                }
            }
        }
    }
}