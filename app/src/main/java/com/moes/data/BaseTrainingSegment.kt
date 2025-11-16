package com.moes.data

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

interface BaseTrainingSegment {
    val startTime: Long
    val endTime: Long?
    val coordinates: List<Coordinate>

    fun distance(): Double {
        if (coordinates.size < 2) return 0.0
        return coordinates.zipWithNext { c1, c2 ->
            calculateDistance(c1, c2)
        }.sum()
    }

    fun duration(): Long {
        return (endTime ?: System.currentTimeMillis()) - startTime
    }

    private fun calculateDistance(c1: Coordinate, c2: Coordinate): Double {
        val earthRadius = 6371000 // meters
        val lat1Rad = Math.toRadians(c1.latitude)
        val lat2Rad = Math.toRadians(c2.latitude)
        val deltaLat = Math.toRadians(c2.latitude - c1.latitude)
        val deltaLon = Math.toRadians(c2.longitude - c1.longitude)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}
