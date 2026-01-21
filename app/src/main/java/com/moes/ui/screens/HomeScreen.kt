package com.moes.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.addOnMoveListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.tripdata.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.tripdata.maneuver.model.Maneuver
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
import com.moes.ui.LocalNetworkStatus
import com.moes.ui.composables.utils.InstructionBanner
import com.moes.ui.composables.utils.SearchBar
import com.moes.ui.composables.utils.TrainingOverlay
import com.moes.ui.viewmodels.HomeViewModel
import com.moes.ui.viewmodels.ProfileViewModel
import com.moes.ui.viewmodels.SocialViewModel
import com.moes.ui.viewmodels.ViewModelFactory
import com.moes.utils.statusBarsPaddingIfOnline
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission", "RestrictedApi")
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    socialViewModel: SocialViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    profileViewModel: ProfileViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateToSummary: (String) -> Unit,
    onNavigateToSocialRequests: () -> Unit
) {
    val navigationRoutes by homeViewModel.navigationRoutes.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()
    val searchSuggestions by homeViewModel.searchSuggestions.collectAsState()
    val trainingState by homeViewModel.trainingState.collectAsState()
    val liveTrainingSession by homeViewModel.liveTrainingSession.collectAsState()
    val finishedSessionId by homeViewModel.finishedSessionId.collectAsState()
    val socialUiState by socialViewModel.uiState.collectAsState()
    val isGuest by profileViewModel.isGuest.collectAsState()
    val incomingRequestsCount = socialUiState.pendingRequests.size

    var instructionText by remember { mutableStateOf("Ricerca percorso...") }
    var distanceText by remember { mutableStateOf("0") }
    var maneuver by remember { mutableStateOf<Maneuver?>(null) }
    var lastEnhancedLocation by remember { mutableStateOf<Location?>(null) }
    var isSearchFocused by remember { mutableStateOf(false) }
    val isSearchActive = isSearchFocused || searchQuery.isNotEmpty()

    val isDarkTheme = isSystemInDarkTheme()
    val mapStyleUri = if (isDarkTheme) Style.DARK else Style.OUTDOORS

    val liveDuration by produceState(
        initialValue = 0L, key1 = liveTrainingSession, key2 = trainingState
    ) {
        while (trainingState == TrainingState.RUNNING) {
            value = liveTrainingSession?.activeDuration() ?: 0L
            delay(500)
        }
        value = liveTrainingSession?.activeDuration() ?: 0L
    }

    val livePace by produceState(
        initialValue = "--:--", key1 = liveTrainingSession, key2 = trainingState
    ) {
        while (trainingState == TrainingState.RUNNING) {
            value = liveTrainingSession?.recentPace() ?: "--:--"
            delay(500)
        }
        value = liveTrainingSession?.recentPace() ?: "--:--"
    }

    val context = LocalContext.current
    val density = LocalDensity.current
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val navigationLocationProvider = remember { NavigationLocationProvider() }
    var circleAnnotationManager by remember { mutableStateOf<CircleAnnotationManager?>(null) }
    var viewportDataSource by remember { mutableStateOf<MapboxNavigationViewportDataSource?>(null) }
    var navigationCamera by remember { mutableStateOf<NavigationCamera?>(null) }

    // Loading state - map is ready when both style is loaded AND we have a valid location
    var isMapStyleLoaded by remember { mutableStateOf(false) }
    var isFirstLocationReceived by remember { mutableStateOf(false) }
    val isMapReady = isMapStyleLoaded && isFirstLocationReceived
    val routeLineApi = remember { MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build()) }
    val routeLineView =
        remember { MapboxRouteLineView(MapboxRouteLineViewOptions.Builder(context).build()) }
    val routeArrowApi = remember { MapboxRouteArrowApi() }
    val routeArrowView =
        remember { MapboxRouteArrowView(RouteArrowOptions.Builder(context).build()) }
    val maneuverApi = remember {
        MapboxManeuverApi(
            MapboxDistanceFormatter(
                DistanceFormatterOptions.Builder(context).build()
            )
        )
    }
    val mapboxNavigation = remember { MapboxNavigationProvider.retrieve() }

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
                { error ->
                    Log.e(
                        "HomeScreen",
                        "Maneuver Error: ${'$'}{error.errorMessage}"
                    )
                },
                { maneuverList ->
                    maneuver = maneuverList.firstOrNull()
                    instructionText = maneuver?.primary?.text ?: "Segui la rotta"
                    val dist = maneuver?.stepDistance?.distanceRemaining
                    distanceText = dist?.toString() ?: "0"
                })
        }
    }

    val locationObserver = remember(trainingState) {
        object : LocationObserver {
            var firstLocationReceivedInternal = false

            override fun onNewRawLocation(rawLocation: Location) {
                if (trainingState == TrainingState.IDLE) {

                    lastEnhancedLocation = rawLocation
                    navigationLocationProvider.changePosition(
                        location = rawLocation, keyPoints = emptyList()
                    )
                    updateCameraIfNeeded(rawLocation)
                }
            }

            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
                if (trainingState != TrainingState.IDLE) {

                    val enhanced = locationMatcherResult.enhancedLocation
                    lastEnhancedLocation = enhanced
                    navigationLocationProvider.changePosition(
                        location = enhanced, keyPoints = locationMatcherResult.keyPoints
                    )

                    viewportDataSource?.onLocationChanged(enhanced)
                    viewportDataSource?.evaluate()

                    updateCameraIfNeeded(enhanced)
                }
            }

            private fun updateCameraIfNeeded(location: Location) {
                if (!firstLocationReceivedInternal) {
                    firstLocationReceivedInternal = true
                    isFirstLocationReceived = true  // Update Compose state for loading overlay
                    mapViewState.value?.camera?.easeTo(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(location.longitude, location.latitude))
                            .bearing(location.bearing).zoom(16.5).build()
                    )
                }
            }
        }
    }

    LaunchedEffect(finishedSessionId) {
        finishedSessionId?.let { id ->
            onNavigateToSummary(id)
            homeViewModel.clearFinishedSessionEvent()
        }
    }

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

    LaunchedEffect(trainingState, mapViewState.value, navigationCamera) {
        val map = mapViewState.value ?: return@LaunchedEffect

        map.location.apply {
            enabled = true
            puckBearingEnabled = trainingState != TrainingState.RUNNING

            puckBearing = when {
                trainingState == TrainingState.IDLE -> PuckBearing.HEADING
                else -> PuckBearing.COURSE
            }
        }

        if (trainingState == TrainingState.RUNNING) {
            navigationCamera?.requestNavigationCameraToFollowing()
        }
    }

    LaunchedEffect(navigationRoutes, trainingState, viewportDataSource) {
        val dataSource = viewportDataSource ?: return@LaunchedEffect

        val bottomPadding = if (trainingState != TrainingState.IDLE) {
            320.dp
        } else {
            180.dp
        }

        dataSource.followingPadding = with(density) {
            EdgeInsets(
                130.dp.toPx().toDouble(),
                24.dp.toPx().toDouble(),
                bottomPadding.toPx().toDouble(),
                24.dp.toPx().toDouble()
            )
        }
        dataSource.evaluate()
    }

    DisposableEffect(locationObserver) {
        mapboxNavigation.registerLocationObserver(locationObserver)
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.startTripSession()
        onDispose {
            mapboxNavigation.unregisterLocationObserver(locationObserver)
            mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
            mapboxNavigation.stopTripSession()
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isOnline = LocalNetworkStatus.current
    val navBarHeight = 64.dp
    val navBarBottomMargin = 16.dp
    val navBarHorizontalMargin = 24.dp
    val bottomObstructionHeight = navBarHeight + navBarBottomMargin

    Box(Modifier.fillMaxSize()) {

        // MAPPA
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx ->
            MapView(ctx).apply {
                compass.enabled = false
                scalebar.enabled = false
                logo.enabled = false
                attribution.enabled = false

                mapboxMap.loadStyle(mapStyleUri) {
                    isMapStyleLoaded = true
                }

                circleAnnotationManager = annotations.createCircleAnnotationManager()
                mapboxMap.setCamera(CameraOptions.Builder().zoom(16.5).build())

                location.apply {
                    setLocationProvider(navigationLocationProvider)
                    locationPuck = createDefault2DPuck(withBearing = true)
                    enabled = true
                    puckBearingEnabled = true
                    puckBearing = PuckBearing.HEADING
                }

                mapViewState.value = this

                viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap).apply {
                    overviewPadding = with(density) {
                        EdgeInsets(
                            130.dp.toPx().toDouble(),
                            24.dp.toPx().toDouble(),
                            180.dp.toPx().toDouble(),
                            24.dp.toPx().toDouble()
                        )
                    }
                    followingPadding = with(density) {
                        EdgeInsets(
                            130.dp.toPx().toDouble(),
                            24.dp.toPx().toDouble(),
                            180.dp.toPx().toDouble(),
                            24.dp.toPx().toDouble()
                        )
                    }
                }

                viewportDataSource?.apply {
                    overviewPitchPropertyOverride(0.0)
                    followingZoomPropertyOverride(16.5)
                    followingPitchPropertyOverride(40.0)
                }

                navigationCamera = NavigationCamera(mapboxMap, camera, viewportDataSource!!)

                mapboxMap.addOnMapLongClickListener { point ->
                    if (trainingState == TrainingState.IDLE) {
                        circleAnnotationManager?.deleteAll()
                        val circleAnnotationOptions =
                            CircleAnnotationOptions().withPoint(point).withCircleRadius(8.0)
                                .withCircleColor("#f06529").withCircleStrokeWidth(2.0)
                                .withCircleStrokeColor("#ffffff")
                        circleAnnotationManager?.create(circleAnnotationOptions)
                        homeViewModel.requestRouteToPoint(point)
                    }
                    true
                }

                mapboxMap.addOnMoveListener(object : OnMoveListener {
                    override fun onMoveBegin(detector: MoveGestureDetector) {
                        navigationCamera?.requestNavigationCameraToIdle()
                        focusManager.clearFocus()  // Clear focus when dragging the map
                    }

                    override fun onMove(detector: MoveGestureDetector): Boolean = false
                    override fun onMoveEnd(detector: MoveGestureDetector) {}
                })

                // Clear focus when tapping on the map
                mapboxMap.addOnMapClickListener {
                    focusManager.clearFocus()
                    true
                }
            }
        }, update = {})

        // LOADING OVERLAY - shows until map style is loaded AND we have a valid location
        AnimatedVisibility(
            visible = !isMapReady,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // SEARCH BAR / INSTRUCTIONS
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPaddingIfOnline(isOnline)
                .padding(
                    top = if (isOnline) 16.dp else 8.dp,
                    start = navBarHorizontalMargin,
                    end = navBarHorizontalMargin
                )
        ) {
            if (trainingState == TrainingState.IDLE) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChanged = { homeViewModel.onSearchQueryChanged(it) },
                        suggestions = searchSuggestions,
                        onSuggestionSelected = {
                            keyboardController?.hide()
                            homeViewModel.onSuggestionSelected(it)
                        },
                        onFocusChange = { isSearchFocused = it },
                        modifier = Modifier.weight(1f)
                    )

                    AnimatedVisibility(
                        visible = !isSearchActive && !isGuest,
                        enter = expandHorizontally(
                            animationSpec = tween(200),
                            expandFrom = Alignment.Start
                        ) + fadeIn(animationSpec = tween(200)),

                        exit = shrinkHorizontally(
                            animationSpec = tween(200),
                            shrinkTowards = Alignment.Start
                        ) + fadeOut(animationSpec = tween(200))
                    ) {
                        Row {
                            Spacer(modifier = Modifier.width(12.dp))

                            Surface(
                                shape = CircleShape,
                                shadowElevation = 8.dp,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { onNavigateToSocialRequests() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (incomingRequestsCount > 0) {
                                                Badge(
                                                    containerColor = MaterialTheme.colorScheme.error,
                                                    contentColor = Color.White
                                                ) {
                                                    Text(text = incomingRequestsCount.toString())
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Richieste Amicizia",
                                            tint = if (incomingRequestsCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (navigationRoutes.isNotEmpty()) {
                InstructionBanner(
                    instruction = instructionText,
                    distanceRemaining = distanceText,
                    maneuverType = maneuver?.primary?.type,
                    maneuverModifier = maneuver?.primary?.modifier
                )
            }
        }

        val trainingBannerHeightApprox = 180.dp
        val buttonsBottomPadding = if (trainingState == TrainingState.IDLE) {
            bottomObstructionHeight + 16.dp
        } else {
            bottomObstructionHeight + trainingBannerHeightApprox + 16.dp
        }

        // LOCATION / CLEAR BUTTONS
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = buttonsBottomPadding, end = navBarHorizontalMargin)
        ) {
            val showClearButton = navigationRoutes.isNotEmpty()

            if (showClearButton) {
                // CLEAR BUTTON
                FloatingActionButton(
                    onClick = {
                        homeViewModel.clearRoute()
                        viewportDataSource?.clearRouteData()
                        circleAnnotationManager?.deleteAll()
                        val currentLoc = lastEnhancedLocation
                        if (currentLoc != null) {
                            mapViewState.value?.camera?.easeTo(
                                CameraOptions.Builder().center(
                                    Point.fromLngLat(
                                        currentLoc.longitude, currentLoc.latitude
                                    )
                                ).zoom(16.5).bearing(0.0).build(),
                            )
                        }
                        navigationCamera?.requestNavigationCameraToOverview()
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Clear Route")
                }
            } else {
                // MY LOCATION BUTTON
                FloatingActionButton(
                    onClick = {
                        // Use lastEnhancedLocation which is consistently updated in both IDLE and training states
                        val currentLoc = lastEnhancedLocation
                        if (currentLoc == null) {
                            Log.d("HomeScreen", "No valid location yet")
                            return@FloatingActionButton
                        }

                        Log.d(
                            "HomeScreen",
                            "Centering to: ${currentLoc.latitude}, ${currentLoc.longitude}"
                        )

                        mapViewState.value?.camera?.easeTo(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(currentLoc.longitude, currentLoc.latitude))
                                .zoom(16.5)
                                .build()
                        )

                        if (trainingState == TrainingState.RUNNING || trainingState == TrainingState.PAUSED) {
                            navigationCamera?.requestNavigationCameraToFollowing()
                        } else {
                            // Use requestNavigationCameraToIdle() in IDLE state to prevent the navigation camera
                            // from overriding our manual camera animation (overview mode without a route
                            // would animate to an empty/default bounding box at 0,0)
                            navigationCamera?.requestNavigationCameraToIdle()
                        }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = "My Location")
                }
            }
        }

        // START BUTTON
        if (trainingState == TrainingState.IDLE) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = buttonsBottomPadding)
            ) {
                FloatingActionButton(
                    onClick = { homeViewModel.onStartTraining() },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                }
            }
        }

        // TRAINING OVERLAY
        if (trainingState == TrainingState.RUNNING || trainingState == TrainingState.PAUSED) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = bottomObstructionHeight + 8.dp)
                    .padding(horizontal = navBarHorizontalMargin)
            ) {
                TrainingOverlay(
                    trainingState = trainingState,
                    duration = liveDuration,
                    distance = liveTrainingSession?.totalDistance() ?: 0.0,
                    pace = livePace,
                    onPauseClick = { homeViewModel.onPauseTraining() },
                    onResumeClick = { homeViewModel.onResumeTraining() },
                    onStopClick = {
                        homeViewModel.onStopTraining()
                        navigationCamera?.requestNavigationCameraToOverview()
                    })
            }
        }
    }
}