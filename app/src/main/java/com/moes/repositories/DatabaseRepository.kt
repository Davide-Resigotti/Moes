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
    suspend fun saveTrainingSession(session: TrainingSession) {
        trainingDao.insertSession(session)

        try {
            firestoreDataSource.saveSession(session)
            trainingDao.markAsSynced(session.id)
        } catch (e: Exception) {
            // Offline
        }
    }

    suspend fun getSessionById(id: String): TrainingSession? = trainingDao.getSessionById(id)

    fun getSessionsForUser(userId: String): Flow<List<TrainingSession>> {
        return trainingDao.getSessionsForUser(userId)
    }

    suspend fun deleteSession(id: String) {
        val session = trainingDao.getSessionById(id)

        if (session != null && session.isSynced) {
            try {
                firestoreDataSource.softDeleteSession(session.userId, id)
            } catch (e: Exception) {
            }
        }

        trainingDao.softDeleteSession(id)
    }

    suspend fun updateSessionTitle(id: String, title: String) {
        trainingDao.updateSessionTitle(id, title)
    }

    fun getUserProfile(userId: String): Flow<UserProfile?> {
        return userDao.getUserProfile(userId)
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        userDao.saveUserProfile(profile)

        if (profile.userId != "moes_guest_user") {
            try {
                firestoreDataSource.saveUserProfile(profile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun migrateGuestData(realUserId: String) {
        withContext(Dispatchers.IO) {
            trainingDao.migrateGuestSessionsToUser(realUserId)
            userDao.migrateGuestProfile(realUserId)

            val migratedProfile = userDao.getUserProfile(realUserId).firstOrNull()
            if (migratedProfile != null) {
                try {
                    firestoreDataSource.saveUserProfile(migratedProfile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    suspend fun syncPendingSessions() {
        val unsyncedSessions = trainingDao.getUnsyncedSessions()

        val successfulIds = mutableListOf<String>()

        unsyncedSessions.forEach { session ->
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
        try {
            val remoteSessions = firestoreDataSource.getSessions(userId)
            remoteSessions.forEach { session ->
                trainingDao.insertSession(session)
            }

            val remoteProfile = firestoreDataSource.getUserProfile(userId)
            if (remoteProfile != null) {
                userDao.saveUserProfile(remoteProfile)
            }
        } catch (e: Exception) {
        }
    }
}