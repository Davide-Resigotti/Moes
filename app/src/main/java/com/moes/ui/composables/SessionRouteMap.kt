package com.moes.ui.composables

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.scalebar.scalebar
import com.moes.utils.PolylineUtils

@Composable
fun SessionRouteMap(
    encodedGeometry: String,
    modifier: Modifier = Modifier
) {
    var isFullscreen by remember { mutableStateOf(false) }

    val coordinates = remember(encodedGeometry) {
        PolylineUtils.decode(encodedGeometry).map { Point.fromLngLat(it.longitude, it.latitude) }
    }

    // VERSIONE EMBEDDED
    Box(modifier = modifier.clip(RoundedCornerShape(16.dp))) {
        InternalMap(
            coordinates = coordinates,
            isInteractive = false,
            modifier = Modifier.fillMaxSize()
        )

        ExpandButton(
            isFullscreen = false,
            onClick = { isFullscreen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    // VERSIONE FULLSCREEN
    if (isFullscreen) {
        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(Modifier.fillMaxSize()) {
                    InternalMap(
                        coordinates = coordinates,
                        isInteractive = true,
                        modifier = Modifier.fillMaxSize()
                    )

                    ExpandButton(
                        isFullscreen = true,
                        onClick = { isFullscreen = false },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 100.dp, end = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandButton(
    isFullscreen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SmallFloatingActionButton(
        onClick = {
            // Fix tap: forza dismiss dialog prima del cambio stato
            if (isFullscreen) {
                onClick()
            } else {
                onClick()
            }
        },
        modifier = modifier,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Icon(
            imageVector = if (isFullscreen) Icons.Default.CloseFullscreen else Icons.Default.OpenInFull,
            contentDescription = if (isFullscreen) "Riduci" else "Espandi"
        )
    }
}

@Composable
private fun InternalMap(
    coordinates: List<Point>,
    isInteractive: Boolean,
    modifier: Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val mapStyleUri = if (isDarkTheme) Style.DARK else Style.OUTDOORS

    val primaryColor = MaterialTheme.colorScheme.primary

    val primaryColorHex = remember(primaryColor) {
        String.format("#%06X", (0xFFFFFF and primaryColor.toArgb()))
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                mapboxMap.loadStyle(mapStyleUri)

                scalebar.enabled = false
                attribution.enabled = false
                compass.enabled = false

                gestures.updateSettings {
                    scrollEnabled = isInteractive
                    rotateEnabled = isInteractive
                    pitchEnabled = isInteractive
                    pinchToZoomEnabled = isInteractive
                    doubleTapToZoomInEnabled = isInteractive
                }
            }
        },
        update = { mapView ->
            if (coordinates.isNotEmpty()) {
                val annotationApi = mapView.annotations
                val polylineManager = annotationApi.createPolylineAnnotationManager()

                polylineManager.deleteAll()

                val polylineAnnotationOptions = PolylineAnnotationOptions()
                    .withPoints(coordinates)
                    .withLineColor(primaryColorHex)
                    .withLineWidth(5.0)

                polylineManager.create(polylineAnnotationOptions)

                mapView.post {
                    val paddingValue = if (isInteractive) 150.0 else 50.0
                    val padding = EdgeInsets(
                        paddingValue, paddingValue, paddingValue, paddingValue
                    )
                    val cameraPosition = mapView.mapboxMap.cameraForCoordinates(
                        coordinates,
                        padding,
                        0.0,
                        0.0
                    )
                    mapView.mapboxMap.setCamera(cameraPosition)
                }
            }
        }
    )
}