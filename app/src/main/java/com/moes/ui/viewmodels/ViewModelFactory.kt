package com.moes.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.moes.MoesApplication

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val app = context.applicationContext as MoesApplication

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                context = context,
                trainingRepository = app.trainingRepository,
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

        if (modelClass.isAssignableFrom(MissionsViewModel::class.java)) {
            return MissionsViewModel(
                app.gamificationRepository,
                app.authRepository
            ) as T
        }

        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                app.authRepository,
                app.databaseRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}