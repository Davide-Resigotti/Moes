package com.moes.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.moes.repositories.DebugDatabaseRepository
import com.moes.repositories.MapboxNavigationRepository
import com.moes.repositories.MapboxSearchRepository
import com.moes.repositories.TrainingRepository

/**
 * A factory for creating ViewModels.
 * This is the single entry point for creating any ViewModel in the app.
 */
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeScreenViewModel::class.java)) {
            // Create the real dependencies for the HomeScreenViewModel
            val databaseRepository = DebugDatabaseRepository()
            val trainingRepository = TrainingRepository(context, databaseRepository)
            val searchRepository = MapboxSearchRepository(context)

            val mapBoxNavigation = MapboxNavigationApp.current()
            val navigationRepository = MapboxNavigationRepository()

            // Create the HomeScreenViewModel with all its dependencies
            @Suppress("UNCHECKED_CAST")
            return HomeScreenViewModel(
                context = context,
                trainingRepository = trainingRepository,
                searchRepository = searchRepository,
                navigationRepository = navigationRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
