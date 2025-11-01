package com.moes.data

import kotlinx.serialization.Serializable

@Serializable
data class TrainingSegment(
    val startTime: Long,
    val endTime: Long,
    val coordinates: List<Coordinate>
)
