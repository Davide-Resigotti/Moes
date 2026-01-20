package com.moes.ui.composables.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moes.data.social.Friend

@Composable
fun FriendsTab(
    friends: List<Friend>,
    onFriendClick: (String) -> Unit,
    onRemoveFriend: (String) -> Unit
) {
    if (friends.isEmpty()) {
        FriendEmptyState(
            icon = Icons.Default.GroupAdd,
            message = "Non hai ancora amici.\nAggiungine uno per confrontare le statistiche!"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.Companion.padding(horizontal = 16.dp)
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