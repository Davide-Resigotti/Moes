package com.moes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moes.ui.composables.social.AddFriendDialog
import com.moes.ui.composables.social.FriendRequestsTab
import com.moes.ui.composables.social.FriendsTab
import com.moes.ui.composables.social.SocialTabBar
import com.moes.ui.viewmodels.SocialViewModel
import com.moes.ui.viewmodels.ViewModelFactory

@Composable
fun SocialScreen(
    initialTab: Int = 0,
    onFriendClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: SocialViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error, uiState.successMessage) {
        if (uiState.successMessage != null) {
            if (showAddFriendDialog) {
                showAddFriendDialog = false
            }
            viewModel.clearMessages()
        }
    }

    if (showAddFriendDialog) {
        AddFriendDialog(
            isLoading = uiState.isLoading,
            error = uiState.error,
            onDismiss = {
                viewModel.clearMessages()
                showAddFriendDialog = false
            },
            onSend = { email ->
                viewModel.sendFriendRequest(email)
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddFriendDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Aggiungi Amico")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp, start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Indietro",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Community",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            SocialTabBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                requestCount = uiState.pendingRequests.size
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && uiState.friends.isEmpty() && uiState.pendingRequests.isEmpty() && !showAddFriendDialog) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    when (selectedTab) {
                        0 -> FriendsTab(
                            friends = uiState.friends,
                            onFriendClick = onFriendClick,
                            onRemoveFriend = { viewModel.removeFriend(it) }
                        )

                        1 -> FriendRequestsTab(
                            requests = uiState.pendingRequests,
                            onAccept = { viewModel.acceptRequest(it) },
                            onReject = { viewModel.rejectRequest(it.id) }
                        )
                    }
                }
            }
        }
    }
}