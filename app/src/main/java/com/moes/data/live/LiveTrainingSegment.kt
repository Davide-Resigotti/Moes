package com.moes.data.live

import com.moes.data.BaseTrainingSegment
import com.moes.data.Coordinate
import com.moes.data.TrainingSegment

data class LiveTrainingSegment(
    override val startTime: Long,
    override val endTime: Long? = null,
    override val coordinates: List<Coordinate> = emptyList()
) : BaseTrainingSegment

fun LiveTrainingSegment.toTrainingSegment(): TrainingSegment {
    return TrainingSegment(
        startTime = this.startTime,
        endTime = this.endTime ?: this.startTime,
        coordinates = this.coordinates
    )
}