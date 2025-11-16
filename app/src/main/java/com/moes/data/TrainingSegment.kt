package com.moes.data

import kotlinx.serialization.Serializable

@Serializable
data class TrainingSegment(
    override val startTime: Long,
    override val endTime: Long,
    override val coordinates: List<Coordinate>
) : BaseTrainingSegment
