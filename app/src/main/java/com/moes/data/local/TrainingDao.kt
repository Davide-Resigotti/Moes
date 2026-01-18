package com.moes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moes.data.TrainingSession
import com.moes.data.UserStatistics
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {
    @Query("SELECT * FROM training_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): TrainingSession?

    @Query("SELECT * FROM training_sessions WHERE userId = :userId AND isDeleted = 0 ORDER BY startTime DESC")
    fun getSessionsForUser(userId: String): Flow<List<TrainingSession>>

    @Query("SELECT * FROM training_sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<TrainingSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TrainingSession)

    @Query("UPDATE training_sessions SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("UPDATE training_sessions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSessionsAsSynced(ids: List<String>)

    @Query("UPDATE training_sessions SET userId = :realUserId, isSynced = 0 WHERE userId = 'moes_guest_user'")
    suspend fun migrateGuestSessionsToUser(realUserId: String)

    @Query("UPDATE training_sessions SET title = :title, isSynced = 0 WHERE id = :id")
    suspend fun updateSessionTitle(id: String, title: String)

    @Query("DELETE FROM training_sessions WHERE id = :id")
    suspend fun hardDeleteSession(id: String)

    @Query("UPDATE training_sessions SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun softDeleteSession(id: String)

    @Query(
        """
        SELECT 
            COUNT(*) as totalSessions, 
            COALESCE(SUM(durationMs), 0) as totalDurationMs, 
            COALESCE(SUM(distanceMeters), 0) as totalDistanceMeters 
        FROM training_sessions 
        WHERE userId = :userId
    """
    )
    fun getUserStatistics(userId: String): Flow<UserStatistics>
}