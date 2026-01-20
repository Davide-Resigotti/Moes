package com.moes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moes.data.social.Friend
import com.moes.data.social.FriendRequest
import com.moes.ui.theme.LogoGradientEnd
import com.moes.ui.theme.LogoGradientStart
import com.moes.ui.viewmodels.SocialViewModel
import com.moes.ui.viewmodels.ViewModelFactory
import com.moes.utils.FormatUtils

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

                        1 -> RequestsTab(
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

// --- TAB AMICI ---
@Composable
fun FriendsTab(
    friends: List<Friend>,
    onFriendClick: (String) -> Unit,
    onRemoveFriend: (String) -> Unit
) {
    if (friends.isEmpty()) {
        EmptyState(
            icon = Icons.Default.GroupAdd,
            message = "Non hai ancora amici.\nAggiungine uno per confrontare le statistiche!"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(friends) { friend ->
                FriendCard(
                    friend = friend,
                    onClick = { onFriendClick(friend.userId) },
                    onRemove = { onRemoveFriend(friend.userId) }
                )
            }
        }
    }
}

// --- TAB RICHIESTE ---
@Composable
fun RequestsTab(
    requests: List<FriendRequest>,
    onAccept: (FriendRequest) -> Unit,
    onReject: (FriendRequest) -> Unit,
) {
    if (requests.isEmpty()) {
        EmptyState(
            icon = Icons.Default.EmojiPeople,
            message = "Nessuna richiesta in sospeso."
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(requests) { request ->
                RequestCard(request = request, onAccept = onAccept, onReject = onReject)
            }
        }
    }
}

// --- COMPONENTI UI ---

@Composable
fun FriendCard(friend: Friend, onClick: () -> Unit, onRemove: () -> Unit) {
    val initials = friend.displayName.take(1).uppercase()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
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
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column {
                Text(
                    text = friend.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Amici dal ${FormatUtils.formatDate(friend.since)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // rimuovi
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.PersonRemove,
                    contentDescription = "Rimuovi amico",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun RequestCard(
    request: FriendRequest,
    onAccept: (FriendRequest) -> Unit,
    onReject: (FriendRequest) -> Unit
) {
    val initials = request.fromUserName.take(1).uppercase()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar piccolo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.fromUserName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Vuole essere tuo amico",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Azioni
            Row {
                IconButton(
                    onClick = { onReject(request) },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Rifiuta")
                }
                IconButton(
                    onClick = { onAccept(request) },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Accetta")
                }
            }
        }
    }
}

@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aggiungi Amico") },
        text = {
            Column {
                Text("Inserisci l'email del tuo amico per inviare una richiesta.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(email) },
                enabled = email.isNotBlank()
            ) {
                Text("Invia")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}