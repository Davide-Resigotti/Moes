package com.moes.ui.composables.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moes.data.social.FriendRequest

// --- TAB RICHIESTE ---
@Composable
fun FriendRequestsTab(
    requests: List<FriendRequest>,
    onAccept: (FriendRequest) -> Unit,
    onReject: (FriendRequest) -> Unit,
) {
    if (requests.isEmpty()) {
        FriendEmptyState(
            icon = Icons.Default.EmojiPeople,
            message = "Nessuna richiesta in sospeso."
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.Companion.padding(horizontal = 16.dp)
        ) {
            items(requests) { request ->
                FriendRequestCard(request = request, onAccept = onAccept, onReject = onReject)
            }
        }
    }
}