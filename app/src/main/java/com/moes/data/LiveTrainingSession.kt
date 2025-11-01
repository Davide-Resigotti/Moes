package com.moes.data

/**
 * Represents a workout that is currently in progress.
 * All properties are immutable to work correctly with StateFlow and Compose.
 */
data class LiveTrainingSession(
    val id: String,
    val startTime: Long,
    val endTime: Long? = null,
    val segments: List<LiveTrainingSegment> = emptyList()
)
