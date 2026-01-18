package com.moes.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.moes.data.TrainingSession
import kotlinx.coroutines.tasks.await

class FirestoreDataSource {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveSession(session: TrainingSession) {
        val sessionToUpload = session.copy(isSynced = true)

        db.collection("users")
            .document(session.userId)
            .collection("sessions")
            .document(session.id)
            .set(sessionToUpload)
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
                // Mappatura manuale aggiornata con TITLE
                TrainingSession(
                    id = doc.getString("id") ?: "",
                    userId = doc.getString("userId") ?: userId,

                    title = doc.getString("title") ?: "Allenamento Recuperato",
                    startTime = doc.getLong("startTime") ?: 0L,
                    endTime = doc.getLong("endTime") ?: 0L,
                    durationMs = doc.getLong("durationMs") ?: 0L,
                    distanceMeters = doc.getDouble("distanceMeters") ?: 0.0,
                    routeGeometry = doc.getString("routeGeometry") ?: "",
                    isSynced = true,
                    isDeleted = doc.getBoolean("isDeleted") ?: false
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun softDeleteSession(userId: String, sessionId: String) {
        db.collection("users")
            .document(userId)
            .collection("sessions")
            .document(sessionId)
            .update("isDeleted", true)
            .await()
    }
}