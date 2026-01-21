package com.moes.data.live

import android.annotation.SuppressLint
import com.moes.data.TrainingSession
import com.moes.utils.PolylineUtils

data class LiveTrainingSession(
    val id: String,
    val startTime: Long,
    val endTime: Long? = null,
    val segments: List<LiveTrainingSegment> = emptyList()
) {
    fun activeDuration(): Long = segments.sumOf { it.duration() }
    fun totalDistance(): Double = segments.sumOf { it.distance() }

    @SuppressLint("DefaultLocale")
    fun pace(): String {
        val dist = totalDistance()
        if (dist == 0.0) return "--:--"
        val secondsPerKm = (activeDuration() / 1000.0) / (dist / 1000.0)
        val min = (secondsPerKm / 60).toInt()
        val sec = (secondsPerKm % 60).toInt()
        return String.format("%02d:%02d", min, sec)
    }

    fun recentPace(windowMillis: Long = 30_000L): String {
        return segments.lastOrNull()?.recentPaceFromPoints(windowMillis) ?: "--:--"
    }

    fun averagePace(): String {
        return segments.lastOrNull()?.averagePace() ?: "--:--"
    }
}

fun LiveTrainingSession.toTrainingSession(userId: String): TrainingSession {
    val allCoordinates = this.segments.flatMap { it.coordinates }

    val encodedGeometry = PolylineUtils.encode(allCoordinates)

    return TrainingSession(
        id = this.id,
        userId = userId,
        startTime = this.startTime,
        endTime = this.endTime ?: System.currentTimeMillis(),
        durationMs = this.activeDuration(),
        distanceMeters = this.totalDistance(),
        routeGeometry = encodedGeometry,
        isSynced = false
    )
}