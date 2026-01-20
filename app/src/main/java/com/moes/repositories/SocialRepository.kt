package com.moes.repositories

import com.moes.data.UserStatistics
import com.moes.data.remote.FirestoreDataSource
import com.moes.data.social.Friend
import com.moes.data.social.FriendRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class SocialRepository(
    private val firestoreDataSource: FirestoreDataSource,
    private val authRepository: AuthRepository,
    private val databaseRepository: DatabaseRepository
) {

    fun getMyFriends(userId: String): Flow<List<Friend>> {
        return if (userId != AuthRepository.GUEST_ID) {
            firestoreDataSource.getFriendsFlow(userId)
        } else {
            emptyFlow()
        }
    }

    fun getIncomingRequests(userId: String): Flow<List<FriendRequest>> {
        return if (userId != AuthRepository.GUEST_ID) {
            firestoreDataSource.getIncomingRequestsFlow(userId)
        } else {
            emptyFlow()
        }
    }

    suspend fun sendRequest(targetEmail: String): Result<Unit> {
        val userId = authRepository.currentUserIdSafe
        if (userId == AuthRepository.GUEST_ID) return Result.failure(Exception("Devi essere loggato"))

        val emailClean = targetEmail.trim().lowercase()

        return try {
            val targetUser = firestoreDataSource.getUserByEmail(emailClean)
                ?: return Result.failure(Exception("Utente non trovato con questa email"))

            if (targetUser.userId == userId) {
                return Result.failure(Exception("Non puoi inviare la richiesta a te stesso"))
            }

            val areAlreadyFriends =
                firestoreDataSource.checkFriendshipExists(userId, targetUser.userId)
            if (areAlreadyFriends) {
                return Result.failure(Exception("Siete già amici!"))
            }

            val pendingOutgoing =
                firestoreDataSource.checkOutgoingRequestExists(userId, targetUser.userId)
            if (pendingOutgoing) {
                return Result.failure(Exception("Hai già inviato una richiesta a questo utente."))
            }

            val pendingIncoming =
                firestoreDataSource.checkIncomingRequestExists(targetUser.userId, userId)
            if (pendingIncoming) {
                return Result.failure(Exception("Questo utente ti ha già inviato una richiesta. Controlla le richieste ricevute."))
            }

            val myProfile = databaseRepository.getUserProfile(userId).firstOrNull()
                ?: return Result.failure(Exception("Impossibile recuperare il tuo profilo."))

            firestoreDataSource.sendFriendRequest(myProfile, targetUser.userId)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun acceptRequest(request: FriendRequest): Result<Unit> {
        val userId = authRepository.currentUserIdSafe
        if (userId == AuthRepository.GUEST_ID) return Result.failure(Exception("Non sei loggato"))

        return try {
            val myProfile = databaseRepository.getUserProfile(userId).firstOrNull()
                ?: return Result.failure(Exception("Errore profilo locale"))

            val myName = "${myProfile.firstName} ${myProfile.lastName}".trim()

            firestoreDataSource.acceptFriendRequest(
                requestId = request.id,
                fromUserId = request.fromUserId,
                myUserId = userId,
                myName = myName.ifBlank { "Amico" },
                friendName = request.fromUserName
            )
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun rejectRequest(requestId: String): Result<Unit> {
        return try {
            firestoreDataSource.rejectFriendRequest(requestId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFriend(friendId: String): Result<Unit> {
        val userId = authRepository.currentUserIdSafe
        if (userId == AuthRepository.GUEST_ID) return Result.failure(Exception("Non sei loggato"))

        return try {
            firestoreDataSource.removeFriend(userId, friendId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFriendStatistics(friendId: String): Flow<UserStatistics?> = flow {
        try {
            val stats = firestoreDataSource.getUserStatistics(friendId)
            emit(stats)
        } catch (e: Exception) {
            emit(null)
        }
    }
}