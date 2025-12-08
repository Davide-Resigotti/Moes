package com.moes

import android.app.Application
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp

/**
 * The Application class for the Moes app.
 * This is the central point for initializing application-wide components.
 */
class MoesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Mapbox Navigation
        if (!MapboxNavigationApp.isSetup()) {
            MapboxNavigationApp.setup(
                NavigationOptions.Builder(this.applicationContext)
                    .build()
            )
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(this.applicationContext)
                    .build()
            )
        }
    }
}
