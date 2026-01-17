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
        // 1. Salva locale
        trainingDao.insertSession(session)

        // 2. Tenta sync col cloud
        try {
            firestoreDataSource.saveSession(session)
            trainingDao.markAsSynced(session.id)
        } catch (e: Exception) {
            // Offline
        }
    }

    fun getSessionsForUser(userId: String): Flow<List<TrainingSession>> {
        return trainingDao.getSessionsForUser(userId)
    }

    suspend fun migrateGuestData(realUserId: String) {
        withContext(Dispatchers.IO) {
            trainingDao.migrateGuestSessionsToUser(realUserId)
        }
    }

    suspend fun syncPendingSessions() {
        // 1. Prendi solo quelle che mancano
        val unsyncedSessions = trainingDao.getUnsyncedSessions()

        // Lista per raccogliere gli ID caricati con successo
        val successfulIds = mutableListOf<String>()

        // 2. Caricale su Firestore
        unsyncedSessions.forEach { session ->
            try {
                firestoreDataSource.saveSession(session)
                // Se il caricamento riesce, aggiungiamo l'ID alla lista (NON aggiorniamo ancora il DB)
                successfulIds.add(session.id)
            } catch (e: Exception) {
                // Se una fallisce, pazienza, non la aggiungiamo alla lista
                e.printStackTrace()
            }
        }

        // 3. Aggiornamento Batch Finale
        // Aggiorniamo il DB locale una sola volta per tutte le sessioni riuscite.
        // Questo fa scattare un solo refresh della UI.
        if (successfulIds.isNotEmpty()) {
            trainingDao.markSessionsAsSynced(successfulIds)
        }
    }

    suspend fun syncFromCloud(userId: String) {
        try {
            // 1. Scarica da Firestore
            val remoteSessions = firestoreDataSource.getSessions(userId)

            // 2. Salva in locale
            remoteSessions.forEach { session ->
                // insertSession usa OnConflictStrategy.REPLACE.
                // Poiché scarichiamo dati "vecchi" ma validi, va bene sovrascrivere
                // o inserire. L'importante è che isSynced sia true (impostato nel DataSource).
                trainingDao.insertSession(session)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getSessionById(id: String): TrainingSession? = trainingDao.getSessionById(id)

    suspend fun deleteSession(id: String) {
        // 1. Recupera la sessione per controllare se va cancellata dal Cloud
        val session = trainingDao.getSessionById(id)

        // 2. Gestione Cloud (Solo se la sessione esiste ed è sincronizzata)
        if (session != null && session.isSynced) {
            try {
                firestoreDataSource.deleteSession(session.userId, id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. CANCELLAZIONE LOCALE (FONDAMENTALE: Eseguila SEMPRE, non usare 'return' prima)
        // Anche se il punto 1 fallisce o ritorna null, noi forziamo la cancellazione dell'ID.
        trainingDao.deleteSessionById(id)
    }

    suspend fun updateSessionTitle(id: String, title: String) {
        trainingDao.updateSessionTitle(id, title)
    }
}