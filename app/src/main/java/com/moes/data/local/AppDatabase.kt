package com.moes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.moes.data.TrainingSession

@Database(entities = [TrainingSession::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trainingDao(): TrainingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Se l'istanza esiste già, la ritorna
            return INSTANCE ?: synchronized(this) {
                // Altrimenti la crea (una volta sola)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moes-database"
                )
                    .fallbackToDestructiveMigration() // Opzionale: utile in sviluppo se cambi il DB
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}