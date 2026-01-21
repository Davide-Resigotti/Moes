package com.moes.repositories

import com.moes.data.TrainingSession
import com.moes.data.UserProfile
import com.moes.data.UserStatistics
import com.moes.data.local.StatisticsDao
import com.moes.data.local.TrainingDao
import com.moes.data.local.UserDao
import com.moes.data.remote.FirestoreDataSource
import com.moes.utils.StatisticsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class DatabaseRepository(
    private val trainingDao: TrainingDao,
    private val userDao: UserDao,
    private val statisticsDao: StatisticsDao,
    private val firestoreDataSource: FirestoreDataSource,
) {
    // --- SESSIONS ---

    fun getSessionsForUser(userId: String): Flow<List<TrainingSession>> {
        return trainingDao.getSessionsForUser(userId)
    }

    suspend fun getSessionById(id: String): TrainingSession? = trainingDao.getSessionById(id)

    suspend fun saveTrainingSession(session: TrainingSession) {
        trainingDao.insertSession(session)

        updateLocalStatistics(session)

        // 3. Sync Cloud
        if (session.userId != AuthRepository.GUEST_ID) {
            try {
                firestoreDataSource.saveSession(session)
                trainingDao.markAsSynced(session.id)

                syncUserStats(session.userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun updateSessionTitle(id: String, title: String) {
        trainingDao.updateSessionTitle(id, title)
        val session = trainingDao.getSessionById(id)

        if (session != null && session.userId != AuthRepository.GUEST_ID) {
            try {
                firestoreDataSource.saveSession(session)
                trainingDao.markAsSynced(id)
            } catch (e: Exception) {
            }
        }
    }

    suspend fun deleteSession(id: String) {
        val session = trainingDao.getSessionById(id) ?: return

        trainingDao.softDeleteSession(id)

        // Recalculate stats immediately to reflect deletion locally
        recalculateUserStatistics(session.userId)

        if (session.userId != AuthRepository.GUEST_ID) {
            try {
                firestoreDataSource.softDeleteSession(session.userId, id)
                trainingDao.markAsSynced(id)
                
                // Sync the updated stats to cloud
                syncUserStats(session.userId)
            } catch (e: Exception) {
            }
        }
    }

    fun getUserProfile(userId: String): Flow<UserProfile?> {
        return userDao.getUserProfile(userId)
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        userDao.saveUserProfile(profile)

        if (profile.userId != AuthRepository.GUEST_ID) {
            try {
                firestoreDataSource.saveUserProfile(profile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getUserStatisticsFlow(userId: String): Flow<UserStatistics?> {
        return statisticsDao.getStatisticsFlow(userId)
    }

    private suspend fun updateLocalStatistics(session: TrainingSession) {
        val currentStats = statisticsDao.getStatistics(session.userId)
        val newStats = StatisticsUtils.calculateNewStatistics(currentStats, session)
        statisticsDao.saveStatistics(newStats)
    }

    suspend fun syncPendingSessions() {
        val unsyncedSessions = trainingDao.getUnsyncedSessions()
        if (unsyncedSessions.isEmpty()) return

        val successfulIds = mutableListOf<String>()

        unsyncedSessions.forEach { session ->
            if (session.userId == AuthRepository.GUEST_ID) return@forEach

            try {
                firestoreDataSource.saveSession(session)
                successfulIds.add(session.id)
            } catch (e: Exception) {
            }
        }

        if (successfulIds.isNotEmpty()) {
            trainingDao.markSessionsAsSynced(successfulIds)
        }
    }

    private suspend fun syncUserStats(userId: String) {
        try {
            val local = statisticsDao.getStatistics(userId)
            val remote = firestoreDataSource.getUserStatistics(userId)

            if (remote == null) {
                if (local != null) firestoreDataSource.saveUserStatistics(local)
            } else {
                if (local != null && local.lastEdited > remote.lastEdited) {
                    firestoreDataSource.saveUserStatistics(local)
                } else {
                    statisticsDao.saveStatistics(remote)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun recalculateUserStatistics(userId: String) {
        val sessions = trainingDao.getAllSessionsForUserSync(userId)
        // Start with clean stats
        var stats = UserStatistics(userId = userId)

        // Replay all sessions to rebuild stats including streaks
        sessions.forEach { session ->
            stats = StatisticsUtils.calculateNewStatistics(stats, session)
        }

        statisticsDao.saveStatistics(stats)
    }

    suspend fun syncFromCloud(userId: String) {
        if (userId == AuthRepository.GUEST_ID) return

        try {
            syncPendingSessions()

            val dirtySessionsIds = trainingDao.getUnsyncedSessions().map { it.id }.toSet()

            val remoteSessions = firestoreDataSource.getSessions(userId)

            remoteSessions.forEach { session ->
                if (session.id !in dirtySessionsIds) {
                    trainingDao.insertSession(session)
                }
            }

            val remoteProfile = firestoreDataSource.getUserProfile(userId)
            val localProfile = userDao.getUserProfile(userId).firstOrNull()

            if (remoteProfile != null) {
                if (localProfile == null || remoteProfile.lastEdited > localProfile.lastEdited) {
                    userDao.saveUserProfile(remoteProfile)
                }
            }

            syncUserStats(userId)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun migrateGuestData(realUserId: String) {
        withContext(Dispatchers.IO) {
            trainingDao.migrateGuestSessionsToUser(realUserId)

            val guestStats = statisticsDao.getStatistics(AuthRepository.GUEST_ID)
            if (guestStats != null) {
                var targetStats = statisticsDao.getStatistics(realUserId)
                try {
                    val remoteStats = firestoreDataSource.getUserStatistics(realUserId)
                    if (remoteStats != null) {
                        targetStats = remoteStats
                    }
                } catch (e: Exception) {
                }

                val mergedStats = StatisticsUtils.mergeStatistics(guestStats, targetStats)
                    .copy(userId = realUserId)

                statisticsDao.saveStatistics(mergedStats)
            }

            statisticsDao.deleteStatistics(AuthRepository.GUEST_ID)

            userDao.migrateGuestProfile(realUserId)

            var localProfile = userDao.getUserProfile(realUserId).firstOrNull()
            try {
                val remoteProfile = firestoreDataSource.getUserProfile(realUserId)
                if (remoteProfile == null) {
                    if (localProfile == null) {
                        localProfile = UserProfile(userId = realUserId)
                        userDao.saveUserProfile(localProfile)
                    }
                    firestoreDataSource.saveUserProfile(localProfile)
                } else {
                    if (localProfile != null && localProfile.lastEdited > remoteProfile.lastEdited) {
                        firestoreDataSource.saveUserProfile(localProfile)
                    } else {
                        userDao.saveUserProfile(remoteProfile)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            userDao.deleteUserProfile(AuthRepository.GUEST_ID)

            syncPendingSessions()
            syncUserStats(realUserId)
        }
    }
}