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

    fun pace(): String {
        val distance = totalDistance()
        if (distance == 0.0) return "0:00"

        val durationInSeconds = activeDuration() / 1000.0
        val paceInSecondsPerKm = durationInSeconds / (distance / 1000.0)

        val minutes = (paceInSecondsPerKm / 60).toInt()
        val seconds = (paceInSecondsPerKm % 60).toInt()

        return String.format("%d:%02d", minutes, seconds)
    }
}
