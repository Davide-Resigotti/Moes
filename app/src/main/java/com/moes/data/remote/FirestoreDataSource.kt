package com.moes.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.moes.data.TrainingSession
import com.moes.data.UserProfile
import com.moes.data.UserStatistics
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

    suspend fun saveUserProfile(profile: UserProfile) {
        db.collection("users")
            .document(profile.userId)
            .collection("data")
            .document("profile")
            .set(profile)
            .await()
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("data")
            .document("profile")
            .get()
            .await()

        return if (snapshot.exists()) {
            try {
                UserProfile(
                    userId = userId,

                    firstName = snapshot.getString("firstName") ?: "",
                    lastName = snapshot.getString("lastName") ?: "",
                    weightKg = snapshot.getDouble("weightKg")?.toFloat() ?: 0f,
                    heightCm = snapshot.getDouble("heightCm")?.toFloat() ?: 0f,
                    gender = snapshot.getString("gender") ?: "M",
                    birthDate = snapshot.getLong("birthDate") ?: 0L,
                    lastEdited = snapshot.getLong("lastEdited") ?: 0L
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    suspend fun saveUserStatistics(stats: UserStatistics) {
        db.collection("users")
            .document(stats.userId)
            .collection("data")
            .document("statistics")
            .set(stats)
            .await()
    }

    suspend fun getUserStatistics(userId: String): UserStatistics? {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("data")
            .document("statistics")
            .get()
            .await()

        return try {
            UserStatistics(
                userId = userId,

                totalSessions = snapshot.getLong("totalSessions")?.toInt() ?: 0,
                totalDurationMs = snapshot.getLong("totalDurationMs") ?: 0L,

                currentStreakDays = snapshot.getLong("currentStreakDays")?.toInt() ?: 0,
                lastTrainingDate = snapshot.getLong("lastTrainingDate") ?: 0L,

                sessionsOver5km = snapshot.getLong("sessionsOver5km")?.toInt() ?: 0,
                sessionsOver10km = snapshot.getLong("sessionsOver10km")?.toInt() ?: 0,

                lastEdited = snapshot.getLong("lastEdited") ?: 0L
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}