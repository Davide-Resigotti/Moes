package com.moes.data.social

data class FriendRequest(
    val id: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val toUserId: String = "",
    val timestamp: Long = 0L
)

data class Friend(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val since: Long = 0L
)