package com.moes.data

import kotlinx.serialization.Serializable

@Serializable
data class TrainingSession(
    val id: String,
    override val segments: List<TrainingSegment>
) : BaseTrainingSession<TrainingSegment>
