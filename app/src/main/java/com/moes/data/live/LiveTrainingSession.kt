package com.moes.data.live

import com.moes.data.BaseTrainingSession

data class LiveTrainingSession(
    val id: String,
    override val segments: List<LiveTrainingSegment> = emptyList()
) : BaseTrainingSession<LiveTrainingSegment>

fun LiveTrainingSession.toTrainingSession(): com.moes.data.TrainingSession {
    return com.moes.data.TrainingSession(
        id = this.id,
        segments = this.segments.map { it.toTrainingSegment() }
    )
}