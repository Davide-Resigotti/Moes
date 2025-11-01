package com.moes.data

/**
 * Represents a segment of a workout that is currently in progress.
 * All properties are immutable to work correctly with StateFlow and Compose.
 */
data class LiveTrainingSegment(
    val startTime: Long,
    val endTime: Long? = null,
    val coordinates: List<Coordinate> = emptyList()
)
