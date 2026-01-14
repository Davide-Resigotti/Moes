package com.moes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moes.data.TrainingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TrainingSession)

    // MODIFICA 1: Prendi solo le sessioni dell'utente specifico (Guest o Reale)
    @Query("SELECT * FROM training_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getSessionsForUser(userId: String): Flow<List<TrainingSession>>

    @Query("SELECT * FROM training_sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<TrainingSession>

    @Query("UPDATE training_sessions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSessionsAsSynced(ids: List<String>)

    @Query("UPDATE training_sessions SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    // MODIFICA 2: Migrazione specifica solo per "moes_guest_user"
    @Query("UPDATE training_sessions SET userId = :realUserId, isSynced = 0 WHERE userId = 'moes_guest_user'")
    suspend fun migrateGuestSessionsToUser(realUserId: String)
}