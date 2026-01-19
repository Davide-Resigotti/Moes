package com.moes.utils

import com.moes.data.TrainingSession
import com.moes.data.UserStatistics
import java.util.Calendar
import kotlin.math.max

object StatisticsUtils {
    fun calculateNewStatistics(
        currentStats: UserStatistics?,
        newSession: TrainingSession
    ): UserStatistics {
        val base = currentStats ?: UserStatistics(userId = newSession.userId)

        val newCurrentStreak = calculateStreak(
            lastDateMs = base.lastTrainingDate,
            newDateMs = newSession.startTime,
            currentStreak = base.currentStreakDays
        )

        val newLongestStreak = max(base.longestStreakDays, newCurrentStreak)

        return base.copy(
            totalSessions = base.totalSessions + 1,
            totalDurationMs = base.totalDurationMs + newSession.durationMs,
            totalDistanceMeters = base.totalDistanceMeters + newSession.distanceMeters,
            currentStreakDays = newCurrentStreak,
            longestStreakDays = newLongestStreak,
            lastTrainingDate = newSession.startTime,
            sessionsOver5km = if (newSession.distanceMeters >= 5000) base.sessionsOver5km + 1 else base.sessionsOver5km,
            sessionsOver10km = if (newSession.distanceMeters >= 10000) base.sessionsOver10km + 1 else base.sessionsOver10km,
            lastEdited = System.currentTimeMillis()
        )
    }

    fun mergeStatistics(guest: UserStatistics, target: UserStatistics?): UserStatistics {
        if (target == null) {
            return guest.copy(userId = guest.userId, lastEdited = System.currentTimeMillis())
        }

        val mergedStats = target.copy(
            totalSessions = target.totalSessions + guest.totalSessions,
            totalDurationMs = target.totalDurationMs + guest.totalDurationMs,
            totalDistanceMeters = target.totalDistanceMeters + guest.totalDistanceMeters,
            sessionsOver5km = target.sessionsOver5km + guest.sessionsOver5km,
            sessionsOver10km = target.sessionsOver10km + guest.sessionsOver10km,
            lastTrainingDate = max(target.lastTrainingDate, guest.lastTrainingDate),
            currentStreakDays = if (guest.lastTrainingDate > target.lastTrainingDate) guest.currentStreakDays else target.currentStreakDays,
            longestStreakDays = max(guest.longestStreakDays, target.longestStreakDays),
            lastEdited = System.currentTimeMillis()
        )
        return mergedStats
    }

    private fun calculateStreak(lastDateMs: Long, newDateMs: Long, currentStreak: Int): Int {
        if (lastDateMs == 0L) return 1

        val lastDate = Calendar.getInstance().apply { timeInMillis = lastDateMs }
        val newDate = Calendar.getInstance().apply { timeInMillis = newDateMs }

        resetTime(lastDate)
        resetTime(newDate)

        val diffDays = (newDate.timeInMillis - lastDate.timeInMillis) / (24 * 60 * 60 * 1000)

        return when (diffDays) {
            0L -> currentStreak
            1L -> currentStreak + 1
            else -> 1
        }
    }

    private fun resetTime(cal: Calendar) {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }
}