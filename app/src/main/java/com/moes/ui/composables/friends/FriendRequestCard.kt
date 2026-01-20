package com.moes.ui.composables.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moes.data.social.FriendRequest

@Composable
fun FriendRequestCard(
    request: FriendRequest,
    onAccept: (FriendRequest) -> Unit,
    onReject: (FriendRequest) -> Unit
) {
    val initials = request.fromUserName.take(1).uppercase()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.Companion.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.Companion.padding(16.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            // Avatar piccolo
            Box(
                modifier = Modifier.Companion
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Companion.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Companion.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.Companion.width(12.dp))

            Column(modifier = Modifier.Companion.weight(1f)) {
                Text(
                    text = request.fromUserName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Companion.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Companion.Ellipsis
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