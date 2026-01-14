package com.moes.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.moes.data.TrainingSession
import kotlinx.coroutines.tasks.await

class FirestoreDataSource {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveSession(session: TrainingSession) {
        // MODIFICA QUI: Creiamo una copia con isSynced = true apposta per il cloud
        val sessionToUpload = session.copy(isSynced = true)

        db.collection("users")
            .document(session.userId) // o sessionToUpload.userId, è uguale
            .collection("sessions")
            .document(session.id)
            .set(sessionToUpload) // Inviamo l'oggetto "pulito"
            .await()
    }

    suspend fun getSessions(userId: String): List<TrainingSession> {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("sessions")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                // Mappatura manuale per sicurezza
                TrainingSession(
                    id = doc.getString("id") ?: "",
                    userId = doc.getString("userId") ?: userId,
                    startTime = doc.getLong("startTime") ?: 0L,
                    endTime = doc.getLong("endTime") ?: 0L,
                    durationMs = doc.getLong("durationMs") ?: 0L,
                    distanceMeters = doc.getDouble("distanceMeters") ?: 0.0,
                    routeGeometry = doc.getString("routeGeometry") ?: "",
                    isSynced = true // Se arriva dal cloud, è già sincronizzato!
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}