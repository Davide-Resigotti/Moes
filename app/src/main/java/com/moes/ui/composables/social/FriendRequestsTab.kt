package com.moes.ui.composables.social

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.Outbound
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moes.data.social.FriendRequest

@Composable
fun FriendRequestsTab(
    receivedRequests: List<FriendRequest>,
    sentRequests: List<FriendRequest>,
    onAccept: (FriendRequest) -> Unit,
    onReject: (FriendRequest) -> Unit,
    onCancelSent: (String) -> Unit
) {
    // 0 = Ricevute, 1 = Inviate
    var selectedSubTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // SELECTOR (Ricevute / Inviate)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // TAB RICEVUTE
            FilterChip(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                label = { Text("Ricevute (${receivedRequests.size})") },
                leadingIcon = {
                    if (selectedSubTab == 0) Icon(
                        Icons.Default.EmojiPeople,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = CircleShape,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedSubTab == 0,
                    borderColor = if (selectedSubTab == 0) Color.Transparent else MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.3f
                    ),
                    borderWidth = 1.dp
                ),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    containerColor = Color.Transparent,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // TAB INVIATE
            FilterChip(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                label = { Text("Inviate (${sentRequests.size})") },
                leadingIcon = {
                    if (selectedSubTab == 1) Icon(
                        Icons.Default.Outbound,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = CircleShape,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedSubTab == 1,
                    borderColor = if (selectedSubTab == 1) Color.Transparent else MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.3f
                    ),
                    borderWidth = 1.dp
                ),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    containerColor = Color.Transparent,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // LISTA
        if (selectedSubTab == 0) {
            // RICEVUTE
            if (receivedRequests.isEmpty()) {
                FriendEmptyState(
                    icon = Icons.Default.EmojiPeople,
                    message = "Nessuna richiesta ricevuta."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(receivedRequests) { request ->
                        FriendRequestCard(
                            request = request,
                            onAccept = onAccept,
                            onReject = onReject
                        )
                    }
                }
            }
        } else {
            // INVIATE
            if (sentRequests.isEmpty()) {
                FriendEmptyState(
                    icon = Icons.Default.Outbound,
                    message = "Non hai inviato nessuna richiesta in attesa."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(sentRequests) { request ->
                        SentRequestCard(
                            request = request,
                            onCancel = onCancelSent
                        )
                    }
                }
            }
        }
    }
}