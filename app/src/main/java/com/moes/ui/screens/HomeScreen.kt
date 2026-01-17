package com.moes.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.moes.ui.composables.InstructionBanner
import com.moes.ui.composables.SearchBar
import com.moes.ui.composables.TrainingOverlay
import com.moes.ui.viewmodels.HomeScreenViewModel
import com.moes.ui.viewmodels.ViewModelFactory
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission", "RestrictedApi")
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateToSummary: (String) -> Unit
) {
    val navigationRoutes by viewModel.navigationRoutes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()
    val trainingState by viewModel.trainingState.collectAsState()
    val liveTrainingSession by viewModel.liveTrainingSession.collectAsState()
    val finishedSessionId by viewModel.finishedSessionId.collectAsState()
    var instructionText by remember { mutableStateOf("") }
    var distanceText by remember { mutableStateOf("") }
    var maneuver by remember { mutableStateOf<Maneuver?>(null) }
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

    val livePace by produceState(
        initialValue = "--:--",
        key1 = liveTrainingSession,
        key2 = trainingState
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
                { error -> Log.e("HomeScreen", "Maneuver Error: ${'$'}{error.errorMessage}") },
                { maneuverList ->
                    maneuver = maneuverList.firstOrNull()
                    instructionText = maneuver?.primary?.text ?: "Follow Route"
                    distanceText = maneuver?.stepDistance?.distanceRemaining.toString()
                }
            )
        }
    }
    val locationObserver = remember(trainingState) {
        object : LocationObserver {
            var firstLocationReceived = false

            // 1. GESTIONE RAW (IDLE)
            override fun onNewRawLocation(rawLocation: Location) {
                if (trainingState == TrainingState.IDLE) {
                    lastEnhancedLocation = rawLocation
                    navigationLocationProvider.changePosition(
                        location = rawLocation,
                        keyPoints = emptyList()
                    )
                    // In IDLE va bene usare easeTo per centrare all'inizio
                    updateCameraIfNeeded(rawLocation)
                }
            }

            // 2. GESTIONE MATCHED (TRAINING)
            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
                if (trainingState != TrainingState.IDLE) {
                    val enhanced = locationMatcherResult.enhancedLocation
                    lastEnhancedLocation = enhanced

                    navigationLocationProvider.changePosition(
                        location = enhanced,
                        keyPoints = locationMatcherResult.keyPoints
                    )

                    viewportDataSource?.onLocationChanged(enhanced)
                    viewportDataSource?.evaluate()

                    updateCameraIfNeeded(enhanced)
                }
            }

            private fun updateCameraIfNeeded(location: Location) {
                if (!firstLocationReceived) {
                    firstLocationReceived = true
                    mapViewState.value?.camera?.easeTo(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(location.longitude, location.latitude))
                            .bearing(location.bearing)
                            .zoom(16.5)
                            .build()
                    )
                }
            }
        }
    }
    LaunchedEffect(finishedSessionId) {
        finishedSessionId?.let { id ->
            onNavigateToSummary(id)
            viewModel.clearFinishedSessionEvent()
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
    LaunchedEffect(trainingState, mapViewState.value) {
        val map = mapViewState.value ?: return@LaunchedEffect

        map.location.apply {
            enabled = true
            puckBearingEnabled = true

            if (trainingState == TrainingState.IDLE) {
                // MODALITÀ IDLE:
                // La freccia indica dove sei girato fisicamente (Bussola/Magnetometro)
                puckBearing = PuckBearing.HEADING
            } else {
                // MODALITÀ TRAINING:
                // La freccia segue la direzione del movimento (GPS)
                // Questo, combinato con la Camera "Following", fa ruotare la mappa
                puckBearing = PuckBearing.COURSE
            }
        }
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

    val navBarHeight = 64.dp
    val navBarBottomMargin = 24.dp
    val navBarHorizontalMargin = 24.dp
    val bottomObstructionHeight = navBarHeight + navBarBottomMargin

    // --- UI LAYOUT ---
    Box(Modifier.fillMaxSize()) {

        // 1. MAPPA
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    compass.enabled = false
                    scalebar.enabled = false
                    logo.enabled = false
                    attribution.enabled = false

                    mapboxMap.loadStyle(Style.OUTDOORS)

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
                                320.dp.toPx().toDouble(),
                                24.dp.toPx().toDouble()
                            )
                        }
                    }

                    viewportDataSource?.apply {
                        // IDLE / OVERVIEW: Zoom alto per vedere dove sono, ma non troppo lontano
                        overviewPitchPropertyOverride(0.0) // Mappa piatta (2D) in idle

                        // TRAINING / FOLLOWING: Zoom molto ravvicinato per vedere le svolte
                        followingZoomPropertyOverride(18.0) // Era 16.5, ora 18.0 (più vicino)
                        followingPitchPropertyOverride(50.0) // Più inclinata per vedere "avanti" (3D)
                    }

                    navigationCamera = NavigationCamera(mapboxMap, camera, viewportDataSource!!)

                    mapboxMap.addOnMapLongClickListener { point ->
                        if (trainingState == TrainingState.IDLE) {
                            // Pulisci vecchi marker
                            circleAnnotationManager?.deleteAll()

                            // Aggiungi marker visivo
                            val circleAnnotationOptions = CircleAnnotationOptions()
                                .withPoint(point)
                                .withCircleRadius(8.0)
                                .withCircleColor("#ee4e8b")
                                .withCircleStrokeWidth(2.0)
                                .withCircleStrokeColor("#ffffff")
                            circleAnnotationManager?.create(circleAnnotationOptions)

                            // Richiedi SUBITO la rotta
                            viewModel.requestRouteToPoint(point)
                        }
                        true
                    }
                    // ... (MoveListener uguale) ...
                    mapboxMap.addOnMoveListener(object : OnMoveListener {
                        override fun onMoveBegin(detector: MoveGestureDetector) {
                            navigationCamera?.requestNavigationCameraToIdle()
                        }

                        override fun onMove(detector: MoveGestureDetector): Boolean = false
                        override fun onMoveEnd(detector: MoveGestureDetector) {}
                    })
                }
            },
            update = {}
        )

        // 2. SEARCH BAR / INSTRUCTIONS
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(
                    top = 16.dp,
                    start = navBarHorizontalMargin, // 24.dp
                    end = navBarHorizontalMargin    // 24.dp
                )
        ) {
            if (trainingState == TrainingState.IDLE) {
                SearchBar(
                    query = searchQuery,
                    onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    suggestions = searchSuggestions,
                    onSuggestionSelected = {
                        keyboardController?.hide()
                        viewModel.onSuggestionSelected(it)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
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

        // 3. LOCATION BUTTON (BIANCO E GRIGIO)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = buttonsBottomPadding, end = navBarHorizontalMargin)
        ) {
            // Condizione: C'è una rotta caricata e NON siamo in allenamento?
            val showClearButton =
                navigationRoutes.isNotEmpty() && trainingState == TrainingState.IDLE

            if (showClearButton) {
                // --- CASO 1: MOSTRA TASTO "CLEAR" (Rosso) ---
                // Sostituisce il tasto location. Usiamo un FAB standard per mantenere la dimensione coerente.
                FloatingActionButton(
                    onClick = {
                        // 1. AZIONI DI PULIZIA ESISTENTI
                        viewModel.clearRoute()
                        viewportDataSource?.clearRouteData()
                        circleAnnotationManager?.deleteAll()

                        // 2. NUOVA AZIONE: RICENTRA LA POSIZIONE
                        val currentLoc = lastEnhancedLocation
                        if (currentLoc != null) {
                            mapViewState.value?.camera?.easeTo(
                                CameraOptions.Builder()
                                    .center(
                                        Point.fromLngLat(
                                            currentLoc.longitude,
                                            currentLoc.latitude
                                        )
                                    )
                                    .zoom(16.5) // Ripristina anche uno zoom comodo
                                    .bearing(0.0) // Ripristina la rotazione a Nord (opzionale, ma pulito)
                                    .build(),
                            )
                        }
                        // Assicura che la camera di navigazione sappia che siamo in "Overview"
                        navigationCamera?.requestNavigationCameraToOverview()
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.errorContainer, // Rosso
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Clear Route")
                }
            } else {
                // --- CASO 2: MOSTRA TASTO "MY LOCATION" (Bianco) ---
                FloatingActionButton(
                    onClick = {
                        val currentLoc = lastEnhancedLocation
                        if (currentLoc != null) {
                            mapViewState.value?.camera?.easeTo(
                                CameraOptions.Builder()
                                    .center(
                                        Point.fromLngLat(
                                            currentLoc.longitude,
                                            currentLoc.latitude
                                        )
                                    )
                                    .build(),
                            )
                        }
                        if (trainingState == TrainingState.RUNNING || trainingState == TrainingState.PAUSED) {
                            navigationCamera?.requestNavigationCameraToFollowing()
                        } else {
                            navigationCamera?.requestNavigationCameraToOverview()
                        }
                    },
                    shape = CircleShape,
                    containerColor = Color.White,
                    contentColor = Color.Gray
                ) {
                    Icon(imageVector = Icons.Default.NearMe, contentDescription = "My Location")
                }
            }
        }

        // 4. START BUTTON (CENTRATO)
        if (trainingState == TrainingState.IDLE) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = buttonsBottomPadding)
            ) {
                FloatingActionButton(
                    onClick = {
                        viewModel.onStartTraining()
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                }
            }
        }

        // 5. TRAINING OVERLAY (SOPRA LA NAVBAR)
        if (trainingState == TrainingState.RUNNING || trainingState == TrainingState.PAUSED) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomObstructionHeight + 8.dp)
                    .padding(horizontal = navBarHorizontalMargin)
            ) {
                TrainingOverlay(
                    trainingState = trainingState,
                    duration = liveDuration,
                    distance = liveTrainingSession?.totalDistance() ?: 0.0,
                    pace = livePace,
                    onPauseClick = { viewModel.onPauseTraining() },
                    onResumeClick = { viewModel.onResumeTraining() },
                    onStopClick = {
                        viewModel.onStopTraining()
                        navigationCamera?.requestNavigationCameraToOverview()
                    }
                )
            }
        }
    }
}