package com.moes.data

interface BaseTrainingSession<T : BaseTrainingSegment> {
    val segments: List<T>

    fun totalDistance(): Double {
        if (segments.isEmpty()) return 0.0
        return segments.sumOf { it.distance() }
    }

    fun activeDuration(): Long {
        if (segments.isEmpty()) return 0L
        return segments.sumOf { it.duration() }
    }

    fun fullDuration(): Long {
        if (segments.isEmpty()) return 0L
        return segments.last().endTime!! - segments.first().startTime
    }
}
