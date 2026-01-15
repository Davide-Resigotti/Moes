package com.moes.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.moes.data.local.AppDatabase
import com.moes.data.remote.FirestoreDataSource
import com.moes.repositories.AuthRepository
import com.moes.repositories.MapboxNavigationRepository
import com.moes.repositories.MapboxSearchRepository
import com.moes.repositories.DatabaseRepository
import com.moes.repositories.TrainingRepository

/**
 * A factory for creating ViewModels.
 * This is the single entry point for creating any ViewModel in the app.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    // 1. Inizializza le dipendenze base
    private val authRepository = AuthRepository()

    private val db = AppDatabase.getDatabase(context)

    // Inizializza Firestore
    private val firestoreDataSource = FirestoreDataSource()

    // 2. Crea il VERO Repository (rispettando la nota: niente authRepository qui!)
    private val databaseRepository = DatabaseRepository(
        trainingDao = db.trainingDao(),
        firestoreDataSource = firestoreDataSource
    )

    // 3. Crea il TrainingRepository passando le dipendenze
    private val trainingRepository = TrainingRepository(context, databaseRepository, authRepository)

    private val searchRepository = MapboxSearchRepository(context)
    private val navigationRepository = MapboxNavigationRepository()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeScreenViewModel::class.java)) {
            return HomeScreenViewModel(
                context = context,
                trainingRepository = trainingRepository,
                searchRepository = searchRepository,
                navigationRepository = navigationRepository
            ) as T
        }

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository, databaseRepository) as T
        }

        if (modelClass.isAssignableFrom(SessionsViewModel::class.java)) {
            return SessionsViewModel( authRepository, databaseRepository) as T
        }

        if (modelClass.isAssignableFrom(SessionDetailViewModel::class.java)) {
            return SessionDetailViewModel(databaseRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}