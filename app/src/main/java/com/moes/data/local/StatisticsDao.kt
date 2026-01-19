package com.moes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moes.data.UserStatistics
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {
    @Query("SELECT * FROM user_statistics WHERE userId = :userId")
    fun getStatisticsFlow(userId: String): Flow<UserStatistics?>

    @Query("SELECT * FROM user_statistics WHERE userId = :userId")
    suspend fun getStatistics(userId: String): UserStatistics?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStatistics(stats: UserStatistics)

    @Query("DELETE FROM user_statistics WHERE userId = :userId")
    suspend fun deleteStatistics(userId: String)
}