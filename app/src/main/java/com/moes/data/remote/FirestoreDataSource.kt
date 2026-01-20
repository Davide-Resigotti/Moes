package com.moes.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.moes.data.TrainingSession
import com.moes.data.UserProfile
import com.moes.data.UserStatistics
import com.moes.data.social.Friend
import com.moes.data.social.FriendRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreDataSource {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveSession(session: TrainingSession) {
        val sessionToUpload = session.copy(isSynced = true)

        db.collection("users").document(session.userId).collection("sessions").document(session.id)
            .set(sessionToUpload).await()
    }

    suspend fun getSessions(userId: String): List<TrainingSession> {
        val snapshot = db.collection("users").document(userId).collection("sessions").get().await()

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
        db.collection("users").document(userId).collection("sessions").document(sessionId)
            .update("isDeleted", true).await()
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        db.collection("users").document(profile.userId).set(profile).await()
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        val snapshot = db.collection("users").document(userId).get().await()

        return if (snapshot.exists()) {
            try {
                UserProfile(
                    userId = userId,
                    firstName = snapshot.getString("firstName") ?: "",
                    lastName = snapshot.getString("lastName") ?: "",
                    email = snapshot.getString("email") ?: "",
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

    suspend fun getUserByEmail(email: String): UserProfile? {
        val snapshot = db.collection("users").whereEqualTo("email", email).limit(1).get().await()

        return if (!snapshot.isEmpty) {
            val doc = snapshot.documents.first()
            UserProfile(
                userId = doc.id,
                firstName = doc.getString("firstName") ?: "",
                lastName = doc.getString("lastName") ?: "",
                email = doc.getString("email") ?: ""
            )
        } else null
    }

    suspend fun saveUserStatistics(stats: UserStatistics) {
        db.collection("users").document(stats.userId).collection("data").document("statistics")
            .set(stats).await()
    }

    suspend fun getUserStatistics(userId: String): UserStatistics? {
        val snapshot =
            db.collection("users").document(userId).collection("data").document("statistics").get()
                .await()

        return try {
            UserStatistics(
                userId = userId,

                totalSessions = snapshot.getLong("totalSessions")?.toInt() ?: 0,
                totalDurationMs = snapshot.getLong("totalDurationMs") ?: 0L,
                totalDistanceMeters = snapshot.getDouble("totalDistanceMeters") ?: 0.0,
                longestStreakDays = snapshot.getLong("longestStreakDays")?.toInt() ?: 0,
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

    suspend fun sendFriendRequest(fromUser: UserProfile, toUserId: String) {
        val request = hashMapOf(
            "fromUserId" to fromUser.userId,
            "fromUserName" to "${fromUser.firstName} ${fromUser.lastName}",
            "toUserId" to toUserId,
            "status" to "PENDING",
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("friend_requests").add(request).await()
    }

    suspend fun acceptFriendRequest(
        requestId: String, fromUserId: String, myUserId: String, myName: String, friendName: String
    ) {
        val batch = db.batch()

        val reqRef = db.collection("friend_requests").document(requestId)
        batch.update(reqRef, "status", "ACCEPTED")

        val myFriendRef =
            db.collection("users").document(myUserId).collection("friends").document(fromUserId)
        batch.set(
            myFriendRef,
            hashMapOf("since" to System.currentTimeMillis(), "displayName" to friendName)
        )

        val otherFriendRef =
            db.collection("users").document(fromUserId).collection("friends").document(myUserId)
        batch.set(
            otherFriendRef,
            hashMapOf("since" to System.currentTimeMillis(), "displayName" to myName)
        )

        batch.commit().await()
    }

    suspend fun rejectFriendRequest(requestId: String) {
        db.collection("friend_requests").document(requestId).update("status", "REJECTED").await()
    }

    suspend fun removeFriend(myId: String, friendId: String) {
        val batch = db.batch()

        val myRef = db.collection("users").document(myId).collection("friends").document(friendId)
        batch.delete(myRef)

        val otherRef =
            db.collection("users").document(friendId).collection("friends").document(myId)
        batch.delete(otherRef)

        batch.commit().await()
    }

    fun getIncomingRequestsFlow(myUserId: String): Flow<List<FriendRequest>> = callbackFlow {
        val registration = db.collection("friend_requests").whereEqualTo("toUserId", myUserId)
            .whereEqualTo("status", "PENDING").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        FriendRequest(
                            id = doc.id,
                            fromUserId = doc.getString("fromUserId") ?: "",
                            fromUserName = doc.getString("fromUserName") ?: "",
                            toUserId = doc.getString("toUserId") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(requests)
            }

        awaitClose { registration.remove() }
    }

    fun getFriendsFlow(userId: String): Flow<List<Friend>> = callbackFlow {
        val registration = db.collection("users").document(userId).collection("friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val friends = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Friend(
                            userId = doc.id,
                            displayName = doc.getString("displayName") ?: "Amico",
                            since = doc.getLong("since") ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(friends)
            }

        awaitClose { registration.remove() }
    }
}