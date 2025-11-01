package com.moes.data

import kotlinx.serialization.Serializable

@Serializable
data class TrainingSession(
    val id: String,
    val startTime: Long,
    val endTime: Long,
    val segments: List<TrainingSegment>
)