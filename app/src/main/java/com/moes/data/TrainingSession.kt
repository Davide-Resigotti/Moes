package com.moes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_sessions")
data class TrainingSession(
    @PrimaryKey val id: String,
    val userId: String,

    val title: String = "Allenamento",
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val distanceMeters: Double,

    val routeGeometry: String,
    val isSynced: Boolean = false
) {
    val avgPaceSeconds: Double
        get() {
            if (distanceMeters <= 0.0) return 0.0
            val durationSec = durationMs / 1000.0
            val distanceKm = distanceMeters / 1000.0
            return durationSec / distanceKm
        }

    val avgSpeedKmh: Double
        get() {
            if (durationMs <= 0) return 0.0
            val distanceKm = distanceMeters / 1000.0
            val durationHours = durationMs / 3600000.0
            return distanceKm / durationHours
        }
}