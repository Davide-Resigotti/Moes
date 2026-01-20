package com.moes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moes.ui.composables.profile.AccountHeader
import com.moes.ui.composables.profile.EditProfileDialog
import com.moes.ui.composables.profile.FriendsSummaryCard
import com.moes.ui.composables.profile.MissionCard
import com.moes.ui.composables.profile.PhysicalStatsCard
import com.moes.ui.viewmodels.MissionsViewModel
import com.moes.ui.viewmodels.ProfileViewModel
import com.moes.ui.viewmodels.SocialViewModel
import com.moes.ui.viewmodels.ViewModelFactory

@Composable
fun AccountScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToSocialFriends: () -> Unit,
    missionsViewModel: MissionsViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    profileViewModel: ProfileViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    socialViewModel: SocialViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val missions by missionsViewModel.missions.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()
    val isGuest by profileViewModel.isGuest.collectAsState()
    val userStats by profileViewModel.userStatistics.collectAsState()
    val socialState by socialViewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditProfileDialog(
            profile = userProfile,
            onDismiss = { showEditDialog = false },
            onSave = { first, last, weight, height, gender, birthDate ->
                profileViewModel.saveProfile(first, last, weight, height, gender, birthDate)
                showEditDialog = false
            })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 130.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // SEZIONE ACCOUNT
            item {
                AccountHeader(
                    profile = userProfile,
                    streak = userStats?.currentStreakDays ?: 0,
                    lastTrainingDate = userStats?.lastTrainingDate ?: 0L,
                    isGuest = isGuest,
                    onAuthClick = {
                        if (isGuest) {
                            onNavigateToAuth()
                        } else {
                            profileViewModel.logout()
                        }
                    }
                )
            }

            // LISTA AMICI
            if (!isGuest) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    FriendsSummaryCard(
                        friendsCount = socialState.friends.size,
                        onClick = onNavigateToSocialFriends
                    )
                }
            }

            // SEZIONE DATI FISICI
            item {
                PhysicalStatsCard(profile = userProfile, onEdit = { showEditDialog = true })
            }

            // SEZIONE MISSIONI
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Le tue Missioni",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(missions) { mission ->
                MissionCard(progress = mission)
            }
        }
    }
}