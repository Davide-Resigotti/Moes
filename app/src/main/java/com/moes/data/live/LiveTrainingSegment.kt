package com.moes.data.live

import com.moes.data.Coordinate
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LiveTrainingSegment(
    val startTime: Long,
    val endTime: Long? = null,
    val coordinates: List<Coordinate> = emptyList()
) {
    fun duration(): Long {
        return (endTime ?: System.currentTimeMillis()) - startTime
    }

    fun distance(): Double {
        if (coordinates.size < 2) return 0.0
        return coordinates.zipWithNext { c1, c2 ->
            calculateDistance(c1, c2)
        }.sum()
    }

    private fun calculateDistance(c1: Coordinate, c2: Coordinate): Double {
        val R = 6371000
        val dLat = Math.toRadians(c2.latitude - c1.latitude)
        val dLon = Math.toRadians(c2.longitude - c1.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(c1.latitude)) * cos(Math.toRadians(c2.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    fun recentPaceFromPoints(windowMillis: Long = 30_000L): String {
        if (coordinates.size < 2) return "--:--"

        val now = System.currentTimeMillis()
        val recentCoords = coordinates.filter {
            now - it.timestamp <= windowMillis
        }

        if (recentCoords.size < 2) return "--:--"

        val recentDistance = recentCoords.zipWithNext { c1, c2 ->
            calculateDistance(c1, c2)
        }.sum()

        val durationSeconds =
            (recentCoords.last().timestamp - recentCoords.first().timestamp) / 1000.0
        if (durationSeconds <= 0.1) return "--:--"

        val paceMinPerKm = (durationSeconds / 60.0) / (recentDistance / 1000.0)

        val minutes = paceMinPerKm.toInt()
        val seconds = ((paceMinPerKm % 1) * 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun averagePace(): String {
        val totalDuration = duration() / 1000.0
        if (totalDuration <= 0.1) return "--:--"

        val totalDistKm = distance() / 1000.0
        val paceMinPerKm = (totalDuration / 60.0) / totalDistKm

        val minutes = paceMinPerKm.toInt()
        val seconds = ((paceMinPerKm % 1) * 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }
}