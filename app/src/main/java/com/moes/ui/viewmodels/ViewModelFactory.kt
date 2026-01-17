package com.moes.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.moes.MoesApplication

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    // Recuperiamo l'applicazione per accedere ai Singleton
    private val app = context.applicationContext as MoesApplication

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeScreenViewModel::class.java)) {
            return HomeScreenViewModel(
                context = context,
                trainingRepository = app.trainingRepository, // Usa l'istanza unica
                searchRepository = app.searchRepository,
                navigationRepository = app.navigationRepository
            ) as T
        }

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(
                app.authRepository,
                app.databaseRepository
            ) as T
        }

        if (modelClass.isAssignableFrom(SessionsViewModel::class.java)) {
            return SessionsViewModel(
                app.authRepository,
                app.databaseRepository
            ) as T
        }

        if (modelClass.isAssignableFrom(SessionDetailViewModel::class.java)) {
            return SessionDetailViewModel(app.databaseRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}