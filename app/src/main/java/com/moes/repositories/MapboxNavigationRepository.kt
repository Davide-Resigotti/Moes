package com.moes.repositories

import android.util.Log
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A repository dedicated to fetching route information from the Mapbox Navigation SDK.
 * It has no knowledge of the UI.
 */
class MapboxNavigationRepository {
    private val mapboxNavigation: MapboxNavigation = MapboxNavigationProvider.retrieve()

    private val _navigationRoutes = MutableStateFlow<List<NavigationRoute>>(emptyList())
    val navigationRoutes: StateFlow<List<NavigationRoute>> = _navigationRoutes

    fun fetchRoute(coordinates: List<Point>) {
        val routeOptions = RouteOptions.builder()
            .coordinatesList(coordinates)
            .profile("mapbox/walking")
            .alternatives(false)
            .build()

        mapboxNavigation.requestRoutes(
            routeOptions,
            callback = object : NavigationRouterCallback {
                override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: String) {
                    _navigationRoutes.value = routes
                }
                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    Log.e("NavigationRepo", "Route failed: $reasons")
                    _navigationRoutes.value = emptyList()
                }
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {
                    _navigationRoutes.value = emptyList()
                }
            }
        )
    }

    fun fetchRoute(origin: Point, destination: Point) {
        fetchRoute(listOf(origin, destination))
    }

    fun clearRoute() {
        _navigationRoutes.value = emptyList()
        mapboxNavigation.setNavigationRoutes(emptyList())
    }
}
