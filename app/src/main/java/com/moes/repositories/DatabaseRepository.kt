package com.moes.repositories

import com.moes.data.TrainingSession
import com.moes.data.UserProfile
import com.moes.data.local.TrainingDao
import com.moes.data.local.UserDao
import com.moes.data.remote.FirestoreDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class DatabaseRepository(
    private val trainingDao: TrainingDao,
    private val userDao: UserDao,
    private val firestoreDataSource: FirestoreDataSource,
) {
    fun getSessionsForUser(userId: String): Flow<List<TrainingSession>> {
        return trainingDao.getSessionsForUser(userId)
    }

    suspend fun getSessionById(id: String): TrainingSession? = trainingDao.getSessionById(id)

    suspend fun saveTrainingSession(session: TrainingSession) {
        trainingDao.insertSession(session)

        if (session.userId != AuthRepository.GUEST_ID) {
            try {
                firestoreDataSource.saveSession(session)
                trainingDao.markAsSynced(session.id)
            } catch (e: Exception) {
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

        if (session.userId != AuthRepository.GUEST_ID) {
            try {
                firestoreDataSource.softDeleteSession(session.userId, id)
                trainingDao.markAsSynced(id)
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun migrateGuestData(realUserId: String) {
        withContext(Dispatchers.IO) {
            trainingDao.migrateGuestSessionsToUser(realUserId)
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
            syncPendingSessions()
        }
    }
}
