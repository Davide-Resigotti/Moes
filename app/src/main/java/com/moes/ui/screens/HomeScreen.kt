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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMoveListener
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

    // --- CACHE LAST LOCATION FOR INSTANT BUTTON RESPONSE ---
    var lastEnhancedLocation by remember { mutableStateOf<Location?>(null) }

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
            viewportDataSource?.onRouteProgressChanged(routeProgress)
            viewportDataSource?.evaluate()

            val style = mapViewState.value?.mapboxMap?.style
            if (style != null) {
                val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
                routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
            }

            val maneuvers = maneuverApi.getManeuvers(routeProgress)
            maneuvers.fold(
                { error -> Log.e("HomeScreen", "Maneuver Error: ${error.errorMessage}") },
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

                // Save this for the button to use later
                lastEnhancedLocation = enhanced

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
            }
        }
    }

    // --- SIDE EFFECTS ---
    LaunchedEffect(navigationRoutes) {
        if (navigationRoutes.isNotEmpty()) {
            mapboxNavigation.setNavigationRoutes(navigationRoutes)
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
            mapboxNavigation.setNavigationRoutes(emptyList())
            val mv = mapViewState.value ?: return@LaunchedEffect
            mv.mapboxMap.style?.let { style ->
                routeLineApi.clearRouteLine { clearRouteLineValue ->
                    routeLineView.renderClearRouteLineValue(style, clearRouteLineValue)
                }
                routeArrowView.render(style, routeArrowApi.clearArrows())
            }
        }
    }

    DisposableEffect(Unit) {
        mapboxNavigation.registerLocationObserver(locationObserver)
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.startTripSession()

        onDispose {
            mapboxNavigation.unregisterLocationObserver(locationObserver)
            mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
            mapboxNavigation.stopTripSession()
            MapboxNavigationProvider.destroy()
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current // Keyboard controller to allow close it when needed

    // --- UI LAYOUT ---
    Box(Modifier.fillMaxSize()) {

        // 1. BACKGROUND: MAP
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    mapboxMap.setCamera(
                        CameraOptions.Builder().zoom(16.0).build()
                    )

                    location.apply {
                        setLocationProvider(navigationLocationProvider)
                        locationPuck = createDefault2DPuck(withBearing = true)
                        enabled = true
                        puckBearingEnabled = true
                        puckBearing = PuckBearing.COURSE
                    }

                    mapViewState.value = this

                    viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap).apply {
                        // Tweak these for "Google Maps" style feel
                        overviewPadding = with(density) {
                            EdgeInsets(100.dp.toPx().toDouble(), 40.dp.toPx().toDouble(), 100.dp.toPx().toDouble(), 40.dp.toPx().toDouble())
                        }
                        followingPadding = with(density) {
                            EdgeInsets(180.dp.toPx().toDouble(), 40.dp.toPx().toDouble(), 60.dp.toPx().toDouble(), 40.dp.toPx().toDouble())
                        }
                    }
                    // 45 degrees pitch is standard for navigation, 0 for overview
                    viewportDataSource?.followingPitchPropertyOverride(45.0)
                    viewportDataSource?.followingZoomPropertyOverride(16.5)

                    val navCamera = NavigationCamera(mapboxMap, camera, viewportDataSource!!)
                    navigationCamera = navCamera

                    mapboxMap.addOnMoveListener(object : OnMoveListener {
                        override fun onMoveBegin(detector: MoveGestureDetector) {
                            navCamera.requestNavigationCameraToIdle()
                        }
                        override fun onMove(detector: MoveGestureDetector): Boolean = false
                        override fun onMoveEnd(detector: MoveGestureDetector) {}
                    })
                }
            },
            update = {}
        )

        // 2. SEARCH BAR OR INSTRUCTIONS
        if (trainingState == TrainingState.IDLE) {
            Box(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                SearchBar(
                    query = searchQuery,
                    onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    suggestions = searchSuggestions,
                    onSuggestionSelected = {
                        keyboardController?.hide() // Hide the keyboard when a suggestion is selected
                        viewModel.onSuggestionSelected(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else if ((trainingState == TrainingState.RUNNING || trainingState == TrainingState.PAUSED) && navigationRoutes.isNotEmpty()) {
            Box(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding()) {
                InstructionBanner(instruction = instructionText, distanceRemaining = distanceText)
            }
        }

        // 3. LOCATION BUTTON
        FloatingActionButton(
            onClick = {
                /** Store the last location avoiding the 1s wait until the location update after the center location button is pressed.*/
                val currentLoc = lastEnhancedLocation
                if (currentLoc != null) {
                    mapViewState.value?.camera?.easeTo(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(currentLoc.longitude, currentLoc.latitude))
                            .build(),
                    )
                }

                /** Using requestNavigationCameraToOverview because it use the 3d view when workout isn't on.*/
                navigationCamera?.requestNavigationCameraToOverview()
            },
            /** Useful postiion in idle mode, should move up when training start to avoid being covered by the training banner */
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 70.dp, end = 16.dp)
        ) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = "My Location")
        }

        // 4. BOTTOM CONTROLS
        if (trainingState == TrainingState.IDLE) {
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (navigationRoutes.isNotEmpty()) {
                    Button(onClick = {
                        viewModel.onStartTraining()
                        navigationCamera?.requestNavigationCameraToFollowing()
                    }) { Text("Start Navigation") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        viewModel.clearRoute()
                        viewportDataSource?.clearRouteData()
                    }) { Text("Clear Route") }
                } else {
                    Button(onClick = {
                        navigationCamera?.requestNavigationCameraToFollowing()
                        viewModel.onStartTraining()
                    }) { Text("Start Workout") }
                }
            }
        }

        if (trainingState == TrainingState.RUNNING || trainingState == TrainingState.PAUSED) {
            Box(modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()) {
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