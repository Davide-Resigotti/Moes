package com.moes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moes.ui.composables.friends.AddFriendDialog
import com.moes.ui.composables.friends.FriendRequestsTab
import com.moes.ui.composables.friends.FriendsTab
import com.moes.ui.viewmodels.SocialViewModel
import com.moes.ui.viewmodels.ViewModelFactory

@Composable
fun SocialScreen(
    initialTab: Int = 0,
    onFriendClick: (String) -> Unit,
    viewModel: SocialViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    // Gestione Messaggi di Errore/Successo
    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    if (showAddFriendDialog) {
        AddFriendDialog(
            onDismiss = { showAddFriendDialog = false },
            onSend = { email ->
                viewModel.sendFriendRequest(email)
                showAddFriendDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (selectedTab == 0) { // Mostra FAB solo nel tab Amici
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
            // --- HEADER TITOLO ---
            Text(
                text = "Community",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 16.dp)
            )

            // --- TABS ---
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Amici", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.People, null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Richieste", fontWeight = FontWeight.SemiBold)
                            if (uiState.pendingRequests.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                // Badge pallino rosso se ci sono richieste
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(MaterialTheme.colorScheme.error, CircleShape)
                                )
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Inbox, null) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CONTENUTO TAB ---
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && uiState.friends.isEmpty() && uiState.pendingRequests.isEmpty()) {
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