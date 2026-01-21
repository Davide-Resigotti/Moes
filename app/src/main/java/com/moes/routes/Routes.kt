package com.moes.routes

object Routes {
    const val HOME = "home"
    const val ACCOUNT = "account"
    const val SESSIONS = "sessions"
    const val SESSION_DETAIL = "session_detail/{sessionId}?source={source}"
    const val AUTH = "auth"

    const val SOCIAL = "social?tab={tab}"
    const val FRIEND_PROFILE = "friend_profile/{friendId}"

    fun sessionDetail(id: String, source: String = "sessions") = "session_detail/$id?source=$source"
    fun friendProfile(id: String) = "friend_profile/$id"

    fun social(tabIndex: Int = 0) = "social?tab=$tabIndex"
}