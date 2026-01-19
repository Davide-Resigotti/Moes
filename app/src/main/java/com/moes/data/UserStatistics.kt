package com.moes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_statistics")
data class UserStatistics(
    @PrimaryKey val userId: String,

    val totalSessions: Int = 0,
    val totalDurationMs: Long = 0L,
    val totalDistanceMeters: Double = 0.0,

    val currentStreakDays: Int = 0,
    val lastTrainingDate: Long = 0L,

    val sessionsOver5km: Int = 0,
    val sessionsOver10km: Int = 0,

    val lastEdited: Long = System.currentTimeMillis()
)