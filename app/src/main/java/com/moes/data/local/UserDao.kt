package com.moes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moes.data.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    fun getUserProfile(userId: String): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Query("UPDATE OR IGNORE user_profile SET userId = :realUserId WHERE userId = 'moes_guest_user'")
    suspend fun migrateGuestProfile(realUserId: String)
}