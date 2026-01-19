package com.moes.repositories

import com.moes.data.UserStatistics
import com.moes.data.local.StatisticsDao
import com.moes.data.missions.MissionProgress
import com.moes.data.missions.MissionType
import com.moes.data.missions.MissionsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GamificationRepository(
    private val statisticsDao: StatisticsDao,
) {
    fun getMissionsProgress(userId: String): Flow<List<MissionProgress>> {
        return statisticsDao.getStatisticsFlow(userId).map { statsOrNull ->
            val stats = statsOrNull ?: UserStatistics(userId = userId)

            MissionsData.allMissions.map { mission ->

                val current = when (mission.type) {
                    MissionType.COUNT -> stats.totalSessions.toLong()
                    MissionType.DISTANCE -> stats.totalDistanceMeters.toLong()
                    MissionType.DURATION -> stats.totalDurationMs
                    MissionType.STREAK -> stats.longestStreakDays.toLong()
                    MissionType.OVER_5K -> stats.sessionsOver5km.toLong()
                    MissionType.OVER_10K -> stats.sessionsOver10km.toLong()
                }

                val nextLevelIndex = mission.levels.indexOfFirst { current < it.threshold }

                val (levelIndex, levelTarget) = if (nextLevelIndex != -1) {
                    nextLevelIndex to mission.levels[nextLevelIndex].threshold
                } else {
                    mission.levels.lastIndex to mission.levels.last().threshold
                }

                val isFullyCompleted = current >= mission.levels.last().threshold

                val progressRaw = if (levelTarget > 0) {
                    current.toDouble() / levelTarget.toDouble()
                } else 0.0

                val progress = progressRaw.coerceIn(0.0, 1.0).toFloat()

                MissionProgress(
                    definition = mission,
                    currentLevelIndex = levelIndex,
                    currentLevelTarget = levelTarget,
                    currentValue = current,
                    progressFloat = progress,
                    isFullyCompleted = isFullyCompleted
                )
            }
        }
    }
}