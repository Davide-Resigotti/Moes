package com.moes

import android.app.Application
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.moes.data.local.AppDatabase
import com.moes.data.remote.FirestoreDataSource
import com.moes.repositories.AuthRepository
import com.moes.repositories.DatabaseRepository
import com.moes.repositories.GamificationRepository
import com.moes.repositories.MapboxNavigationRepository
import com.moes.repositories.MapboxSearchRepository
import com.moes.repositories.TrainingRepository

class MoesApplication : Application() {

    // ISTANZE UNICHE (SINGLETON)
    lateinit var database: AppDatabase
    lateinit var authRepository: AuthRepository
    lateinit var databaseRepository: DatabaseRepository
    lateinit var trainingRepository: TrainingRepository
    lateinit var searchRepository: MapboxSearchRepository
    lateinit var navigationRepository: MapboxNavigationRepository
    lateinit var gamificationRepository: GamificationRepository

    override fun onCreate() {
        super.onCreate()

        // 1. Mapbox
        MapboxNavigationProvider.create(
            NavigationOptions.Builder(this.applicationContext)
                .build()
        )

        // 2. Database & Auth
        database = AppDatabase.getDatabase(this)
        val firestoreDataSource = FirestoreDataSource()
        authRepository = AuthRepository()

        // 3. Repositories
        databaseRepository =
            DatabaseRepository(database.trainingDao(), database.userDao(), firestoreDataSource)

        // Questo è il punto critico: TrainingRepository creato UNA SOLA VOLTA
        trainingRepository = TrainingRepository(this, databaseRepository, authRepository)

        searchRepository = MapboxSearchRepository(this)
        navigationRepository = MapboxNavigationRepository()
        gamificationRepository = GamificationRepository(database.trainingDao(), authRepository)
    }
}