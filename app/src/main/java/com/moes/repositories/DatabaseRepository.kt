package com.moes.repositories

import com.moes.data.TrainingSession
import com.moes.data.local.TrainingDao
import com.moes.data.remote.FirestoreDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DatabaseRepository(
    private val trainingDao: TrainingDao,
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

    suspend fun migrateGuestData(realUserId: String) {
        withContext(Dispatchers.IO) {
            trainingDao.migrateGuestSessionsToUser(realUserId)
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
        } catch (e: Exception) {
        }
    }
}