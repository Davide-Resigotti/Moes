package com.moes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moes.ui.composables.EditProfileDialog
import com.moes.ui.composables.MissionCard
import com.moes.ui.composables.UserProfileCard
import com.moes.ui.viewmodels.MissionsViewModel
import com.moes.ui.viewmodels.ProfileViewModel
import com.moes.ui.viewmodels.ViewModelFactory

@Composable
fun AccountScreen(
    onNavigateToAuth: () -> Unit,
    missionsViewModel: MissionsViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    profileViewModel: ProfileViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val missions by missionsViewModel.missions.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()
    val isGuest by profileViewModel.isGuest.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditProfileDialog(
            profile = userProfile,
            onDismiss = { showEditDialog = false },
            onSave = { first, last, weight, height, gender ->
                profileViewModel.saveProfile(first, last, weight, height, gender)
                showEditDialog = false
            }
        )
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
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- SEZIONE PROFILO ---
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profilo",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                UserProfileCard(
                    profile = userProfile,
                    isGuest = isGuest, // Passiamo lo stato
                    onMainActionClick = {
                        if (isGuest) {
                            onNavigateToAuth() // Va al login
                        } else {
                            profileViewModel.logout() // Esegue logout
                        }
                    },
                    onEdit = { showEditDialog = true }
                )
            }

            // --- SEZIONE MISSIONI ---
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