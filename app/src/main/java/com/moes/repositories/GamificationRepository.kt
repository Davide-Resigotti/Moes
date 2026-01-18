package com.moes.repositories

import com.moes.data.local.TrainingDao
import com.moes.data.missions.MissionProgress
import com.moes.data.missions.MissionType
import com.moes.data.missions.MissionsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GamificationRepository(
    private val trainingDao: TrainingDao,
    private val authRepository: AuthRepository
) {

    fun getMissionsProgress(): Flow<List<MissionProgress>> {
        val userId = authRepository.currentUserIdSafe

        return trainingDao.getUserStatistics(userId).map { stats ->
            MissionsData.allMissions.map { mission ->

                val current = when (mission.type) {
                    MissionType.COUNT -> stats.totalSessions.toLong()
                    MissionType.DISTANCE -> stats.totalDistanceMeters.toLong()
                    MissionType.DURATION -> stats.totalDurationMs
                }

                val progressRaw = if (mission.threshold > 0) {
                    current.toDouble() / mission.threshold.toDouble()
                } else 0.0
                val progress = progressRaw.coerceIn(0.0, 1.0).toFloat()

                MissionProgress(
                    definition = mission,
                    currentValue = current,
                    isCompleted = current >= mission.threshold,
                    progressFloat = progress
                )
            }
        }
    }
}