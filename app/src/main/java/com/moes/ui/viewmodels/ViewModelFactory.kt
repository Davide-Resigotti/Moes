package com.moes.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.moes.repositories.DebugDatabaseRepository
import com.moes.repositories.DebugSearchRepository
import com.moes.repositories.TrainingRepository

/**
 * A factory for creating ViewModels.
 * This is the single entry point for creating any ViewModel in the app.
 */
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeScreenViewModel::class.java)) {
            // Create the dependencies for the HomeScreenViewModel
            val databaseRepository = DebugDatabaseRepository()
            val trainingRepository = TrainingRepository(context, databaseRepository)
            val searchRepository = DebugSearchRepository()
            
            // Create the HomeScreenViewModel
            @Suppress("UNCHECKED_CAST")
            return HomeScreenViewModel(trainingRepository, searchRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
