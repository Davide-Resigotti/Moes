package com.moes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_sessions")
data class TrainingSession(
    @PrimaryKey val id: String,
    val userId: String,

    // Dati statistici
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val distanceMeters: Double,

    // La rotta compressa
    val routeGeometry: String,

    // Sync
    val isSynced: Boolean = false
)