package com.moes.routes

object Routes {
    const val HOME = "home"
    const val ACCOUNT = "account"
    const val SESSIONS = "sessions"
    // Nuova rotta con argomento
    const val SESSION_DETAIL = "session_detail/{sessionId}?isPostWorkout={isPostWorkout}"

    // Helper per costruire la rotta
    fun sessionDetail(id: String, isPostWorkout: Boolean = false) =
        "session_detail/$id?isPostWorkout=$isPostWorkout"
}