package com.moes.routes

object Routes {
    const val HOME = "home"
    const val ACCOUNT = "account"
    const val SESSIONS = "sessions"

    // MODIFICA: Rimossa la parte ?isPostWorkout=...
    const val SESSION_DETAIL = "session_detail/{sessionId}"

    // Helper semplificato
    fun sessionDetail(id: String) = "session_detail/$id"
}