package com.moes.ui.composables

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.moes.utils.PolylineUtils

@Composable
fun SessionRouteMap(
    encodedGeometry: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Decodifica la geometria usando la tua utility esistente
    val coordinates = remember(encodedGeometry) {
        PolylineUtils.decode(encodedGeometry).map { Point.fromLngLat(it.longitude, it.latitude) }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                // Carica lo stile
                mapboxMap.loadStyle(Style.OUTDOORS)

                // Disabilita interazioni se vuoi solo visualizzare (opzionale)
                gestures.updateSettings {
                    scrollEnabled = true
                    rotateEnabled = false
                    pitchEnabled = false
                    pinchToZoomEnabled = true
                }
            }
        },
        update = { mapView ->
            if (coordinates.isNotEmpty()) {
                val annotationApi = mapView.annotations
                val polylineManager = annotationApi.createPolylineAnnotationManager()

                // Pulisci vecchie annotazioni per evitare duplicati
                polylineManager.deleteAll()

                // Disegna la linea
                val polylineAnnotationOptions = PolylineAnnotationOptions()
                    .withPoints(coordinates)
                    .withLineColor("#F59B23") // Tuo colore Orange
                    .withLineWidth(5.0)

                polylineManager.create(polylineAnnotationOptions)

                // FIX FONDAMENTALE: Usiamo .post {}
                // Questo assicura che il calcolo avvenga DOPO che la view ha dimensioni reali.
                mapView.post {
                    // Padding: Margine dai bordi (sx, alto, dx, basso) in pixel.
                    // Aumentato a 100.0 per evitare che la linea tocchi i bordi o finisca sotto le barre.
                    val padding = EdgeInsets(100.0, 100.0, 100.0, 100.0)

                    // FIX DEPRECATION: Passiamo esplicitamente bearing e pitch a 0.0
                    // Mapbox ora richiede questi parametri per evitare ambiguità.
                    val cameraPosition = mapView.mapboxMap.cameraForCoordinates(
                        coordinates,
                        padding,
                        0.0, // Bearing (Rotazione)
                        0.0  // Pitch (Inclinazione)
                    )

                    // Applica la camera calcolata
                    mapView.mapboxMap.setCamera(cameraPosition)
                }
            }
        }
    )
}