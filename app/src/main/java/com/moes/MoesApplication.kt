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
import com.moes.repositories.SocialRepository
import com.moes.repositories.TrainingRepository

class MoesApplication : Application() {

    lateinit var database: AppDatabase
    lateinit var authRepository: AuthRepository
    lateinit var databaseRepository: DatabaseRepository
    lateinit var trainingRepository: TrainingRepository
    lateinit var searchRepository: MapboxSearchRepository
    lateinit var navigationRepository: MapboxNavigationRepository
    lateinit var gamificationRepository: GamificationRepository
    lateinit var socialRepository: SocialRepository

    override fun onCreate() {
        super.onCreate()

        MapboxNavigationProvider.create(
            NavigationOptions.Builder(this.applicationContext)
                .build()
        )

        database = AppDatabase.getDatabase(this)
        val firestoreDataSource = FirestoreDataSource()

        authRepository = AuthRepository()
        databaseRepository =
            DatabaseRepository(
                database.trainingDao(),
                database.userDao(),
                database.statisticsDao(),
                database,
                firestoreDataSource
            )
        trainingRepository = TrainingRepository(this, databaseRepository, authRepository)
        searchRepository = MapboxSearchRepository(this)
        navigationRepository = MapboxNavigationRepository()
        gamificationRepository = GamificationRepository(database.statisticsDao())
        socialRepository = SocialRepository(firestoreDataSource, authRepository, databaseRepository)
    }
}