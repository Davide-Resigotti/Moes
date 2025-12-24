package com.moes.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.tripdata.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.moes.data.TrainingState
import com.moes.ui.composables.InstructionBanner
import com.moes.ui.composables.SearchBar
import com.moes.ui.composables.TrainingOverlay
import com.moes.ui.viewmodels.HomeScreenViewModel
import com.moes.ui.viewmodels.ViewModelFactory
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeScreenViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val navigationRoutes by viewModel.navigationRoutes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()
    val trainingState by viewModel.trainingState.collectAsState()
    val liveTrainingSession by viewModel.liveTrainingSession.collectAsState()

    // --- UI STATE FOR INSTRUCTIONS ---
    var instructionText by remember { mutableStateOf("") }
    var distanceText by remember { mutableStateOf("") }

    val liveDuration by produceState(
        initialValue = 0L,
        key1 = liveTrainingSession,
        key2 = trainingState
    ) {
        while (trainingState == TrainingState.RUNNING) {
            value = liveTrainingSession?.activeDuration() ?: 0L
            delay(500)
        }
        value = liveTrainingSession?.activeDuration() ?: 0L
    }

    val context = LocalContext.current
    val density = LocalDensity.current

    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val navigationLocationProvider = remember { NavigationLocationProvider() }

    var viewportDataSource by remember { mutableStateOf<MapboxNavigationViewportDataSource?>(null) }
    var navigationCamera by remember { mutableStateOf<NavigationCamera?>(null) }

    // --- MAPBOX API INITIALIZATION ---
    val routeLineApi = remember {
        MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
    }
    val routeLineView = remember {
        MapboxRouteLineView(MapboxRouteLineViewOptions.Builder(context).build())
    }

    val routeArrowApi = remember { MapboxRouteArrowApi() }
    val routeArrowView = remember {
        MapboxRouteArrowView(RouteArrowOptions.Builder(context).build())
    }

    // Helper to format distance and extract text
    val maneuverApi = remember {
        MapboxManeuverApi(
            MapboxDistanceFormatter(DistanceFormatterOptions.Builder(context).build())
        )
    }

    val mapboxNavigation = remember {
        MapboxNavigationProvider.retrieve();
    }

    // --- OBSERVERS ---
    val routeProgressObserver = remember {
        RouteProgressObserver { routeProgress ->
            // 1. Update Camera
            viewportDataSource?.onRouteProgressChanged(routeProgress)
            viewportDataSource?.evaluate()

            // 2. Update Arrows on Map
            val style = mapViewState.value?.mapboxMap?.style
            if (style != null) {
                val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
                routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
            }

            // 3. Update UI Instructions
            val maneuvers = maneuverApi.getManeuvers(routeProgress)
            maneuvers.fold(
                { error ->
                    Log.e("HomeScreen", "Maneuver Error: ${error.errorMessage}")
                },
                { maneuverList ->
                    val nextManeuver = maneuverList.firstOrNull()
                    instructionText = nextManeuver?.primary?.text ?: "Follow Route"
                    distanceText = nextManeuver?.stepDistance?.distanceRemaining.toString()
                }
            )
        }
    }

    val locationObserver = remember {
        object : LocationObserver {
            var firstLocationReceived = false

            override fun onNewRawLocation(rawLocation: Location) {}

            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
                val enhanced = locationMatcherResult.enhancedLocation

                navigationLocationProvider.changePosition(
                    location = enhanced,
                    keyPoints = locationMatcherResult.keyPoints
                )
                viewportDataSource?.onLocationChanged(enhanced)
                viewportDataSource?.evaluate()

                if (!firstLocationReceived) {
                    firstLocationReceived = true
                    mapViewState.value?.camera?.easeTo(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(enhanced.longitude, enhanced.latitude))
                            .bearing(enhanced.bearing)
                            .zoom(16.0)
                            .build()
                    )
                }

                if (trainingState == TrainingState.RUNNING) {
                    navigationCamera?.requestNavigationCameraToFollowing()
                }
            }
        }
    }

    // --- SIDE EFFECTS ---

    LaunchedEffect(navigationRoutes) {
        if (navigationRoutes.isNotEmpty()) {
            mapboxNavigation.setNavigationRoutes(navigationRoutes)

            // Draw the route on the map
            val mv = mapViewState.value ?: return@LaunchedEffect
            routeLineApi.setNavigationRoutes(navigationRoutes) { drawData ->
                mv.mapboxMap.style?.let { style ->
                    routeLineView.renderRouteDrawData(style, drawData)
                }
            }
            viewportDataSource?.onRouteChanged(navigationRoutes.first())
            viewportDataSource?.evaluate()
            navigationCamera?.requestNavigationCameraToOverview()
        } else {
            // Clear the route from the map
            mapboxNavigation.setNavigationRoutes(emptyList())
            val mv = mapViewState.value ?: return@LaunchedEffect
            mv.mapboxMap.style?.let { style ->
                routeLineApi.clearRouteLine { clearRouteLineValue ->
                    routeLineView.renderClearRouteLineValue(style, clearRouteLineValue)
                }
                // Also clear arrows
                routeArrowView.render(style, routeArrowApi.clearArrows())
            }
        }
    }

    DisposableEffect(Unit) {
        mapboxNavigation.registerLocationObserver(locationObserver)
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver) // Register Progress
        mapboxNavigation.startTripSession()

        onDispose {
            mapboxNavigation.unregisterLocationObserver(locationObserver)
            mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver) // Unregister
            mapboxNavigation.stopTripSession()
            MapboxNavigationProvider.destroy()
        }
    }

    // --- UI LAYOUT ---

    Box(Modifier.fillMaxSize()) {

        // 1. BACKGROUND: MAP
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .zoom(16.0)
                            .build()
                    )

                    location.apply {
                        setLocationProvider(navigationLocationProvider)
                        locationPuck = createDefault2DPuck(withBearing = true)
                        enabled = true
                        puckBearingEnabled = true
                        puckBearing = PuckBearing.COURSE // Fixes the rotation issue
                    }

                    mapViewState.value = this

                    viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap).apply {
                        overviewPadding = with(density) {
                            EdgeInsets(
                                100.dp.toPx().toDouble(),
                                40.dp.toPx().toDouble(),
                                100.dp.toPx().toDouble(),
                                40.dp.toPx().toDouble()
                            )
                        }
                        followingPadding = with(density) {
                            EdgeInsets(
                                // Use HIGH Top padding to push the puck DOWN to the bottom
                                180.dp.toPx().toDouble(),
                                40.dp.toPx().toDouble(),
                                // Use LOW Bottom padding so we can see the road ahead
                                60.dp.toPx().toDouble(),
                                40.dp.toPx().toDouble()
                            )
                        }
                    }
                    viewportDataSource?.followingPitchPropertyOverride(45.0)
                    viewportDataSource?.followingZoomPropertyOverride(18.0)

                    navigationCamera = NavigationCamera(mapboxMap, camera, viewportDataSource!!)
                }
            },
            update = {}
        )

        // 2. OVERLAY: TOP AREA (Search or Instructions)
        if (trainingState == TrainingState.IDLE) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding() // Padding for status bar
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    suggestions = searchSuggestions,
                    onSuggestionSelected = { viewModel.onSuggestionSelected(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else if ((trainingState == TrainingState.RUNNING || trainingState == TrainingState.PAUSED) && navigationRoutes.isNotEmpty()) {
            // Show Instructions when Running AND we have a route
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            ) {
                InstructionBanner(
                    instruction = instructionText,
                    distanceRemaining = distanceText,
                )
            }
        }

        // 3. OVERLAY: BOTTOM AREA (Buttons or Stats)
        if (trainingState == TrainingState.IDLE) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding() // Padding for nav bar
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (navigationRoutes.isNotEmpty()) {
                    Button(onClick = {
                        // 1. Change State
                        viewModel.onStartTraining()

                        // 2. Force Camera Update IMMEDIATELY
                        navigationCamera?.requestNavigationCameraToFollowing()

                        // 3. Optional: Manually center on the last known location right now
                        // This prevents the "waiting for next GPS signal" lag
                        navigationLocationProvider.lastLocation?.let { loc ->
                            viewportDataSource?.onLocationChanged(loc)
                            viewportDataSource?.evaluate()
                        }
                    }) {
                        Text("Start Navigation")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        viewModel.clearRoute()
                        viewportDataSource?.clearRouteData()
                    }) {
                        Text("Clear Route")
                    }
                } else {
                    Button(onClick = {
                        navigationCamera?.requestNavigationCameraToFollowing()
                        viewModel.onStartTraining()
                    }) {
                        Text("Start Workout")
                    }
                }
            }
        }

        if (trainingState == TrainingState.RUNNING || trainingState == TrainingState.PAUSED) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            ) {
                TrainingOverlay(
                    trainingState = trainingState,
                    duration = liveDuration,
                    distance = liveTrainingSession?.totalDistance() ?: 0.0,
                    onPauseClick = { viewModel.onPauseTraining() },
                    onResumeClick = { viewModel.onResumeTraining() },
                    onStopClick = { viewModel.onStopTraining() }
                )
            }
        }
    }
}