package com.moes.utils

import com.moes.data.TrainingSession
import com.moes.data.UserStatistics
import java.util.Calendar
import kotlin.math.max

object StatisticsUtils {
    fun calculateNewStatistics(
        currentStats: UserStatistics?, newSession: TrainingSession
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

        val (oldStats, newStats) = if (guest.lastTrainingDate > target.lastTrainingDate) {
            target to guest
        } else {
            guest to target
        }

        val diffDays = getDiffDays(oldStats.lastTrainingDate, newStats.lastTrainingDate)

        val mergedCurrentStreak = if (diffDays == newStats.currentStreakDays.toLong()) {
            oldStats.currentStreakDays + newStats.currentStreakDays
        } else {
            newStats.currentStreakDays
        }

        val mergedStats = target.copy(
            totalSessions = target.totalSessions + guest.totalSessions,
            totalDurationMs = target.totalDurationMs + guest.totalDurationMs,
            totalDistanceMeters = target.totalDistanceMeters + guest.totalDistanceMeters,
            sessionsOver5km = target.sessionsOver5km + guest.sessionsOver5km,
            sessionsOver10km = target.sessionsOver10km + guest.sessionsOver10km,
            lastTrainingDate = newStats.lastTrainingDate,
            currentStreakDays = mergedCurrentStreak,
            longestStreakDays = max(
                max(guest.longestStreakDays, target.longestStreakDays), mergedCurrentStreak
            ),
            lastEdited = System.currentTimeMillis()
        )
        return mergedStats
    }

    private fun calculateStreak(lastDateMs: Long, newDateMs: Long, currentStreak: Int): Int {
        if (lastDateMs == 0L) return 1
        val diffDays = getDiffDays(lastDateMs, newDateMs)

        return when (diffDays) {
            0L -> currentStreak
            1L -> currentStreak + 1
            else -> 1
        }
    }

    private fun getDiffDays(date1Ms: Long, date2Ms: Long): Long {
        val cal1 = Calendar.getInstance().apply { timeInMillis = date1Ms }
        val cal2 = Calendar.getInstance().apply { timeInMillis = date2Ms }

        resetTime(cal1)
        resetTime(cal2)

        return (cal2.timeInMillis - cal1.timeInMillis) / (24 * 60 * 60 * 1000)
    }

    private fun resetTime(cal: Calendar) {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }

    fun getStreakStatus(streak: Int, lastTrainingDate: Long): Pair<Boolean, String> {
        if (streak <= 0) return false to ""

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        cal.timeInMillis = now
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)
        val todayYear = cal.get(Calendar.YEAR)
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)

        cal.timeInMillis = lastTrainingDate
        val lastDay = cal.get(Calendar.DAY_OF_YEAR)
        val lastYear = cal.get(Calendar.YEAR)

        val isTrainedToday = (todayDay == lastDay && todayYear == lastYear)

        return if (isTrainedToday) {
            when {
                streak >= 100 -> true to "💯"
                streak >= 10 -> true to "🥳"
                else -> true to "🔥"
            }
        } else {
            val hoursLeft = 24 - currentHour
            when {
                hoursLeft <= 2 -> true to "⌛"
                hoursLeft <= 5 -> true to "⏳"
                streak >= 100 -> true to "💯"
                streak >= 10 -> true to "🥳"
                else -> true to "🔥"
            }
        }
    }
}