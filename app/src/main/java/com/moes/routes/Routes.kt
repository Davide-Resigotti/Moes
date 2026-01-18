package com.moes.routes

object Routes {
    const val HOME = "home"
    const val ACCOUNT = "account"
    const val SESSIONS = "sessions"
    const val SESSION_DETAIL = "session_detail/{sessionId}"
    const val AUTH = "auth"

    fun sessionDetail(id: String) = "session_detail/$id"
}