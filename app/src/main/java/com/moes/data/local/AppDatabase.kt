package com.moes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.moes.data.TrainingSession
import com.moes.data.UserProfile
import com.moes.data.UserStatistics

@Database(
    entities = [TrainingSession::class, UserProfile::class, UserStatistics::class],
    version = 6
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trainingDao(): TrainingDao
    abstract fun userDao(): UserDao
    abstract fun statisticsDao(): StatisticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moes-database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}