package com.moes.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column // Import necessario
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider // Import
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState // Import
import androidx.compose.runtime.compositionLocalOf // Import
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.content.ContextCompat
import com.moes.ui.composables.utils.OfflineBanner
import com.moes.ui.navigation.MoesNavHost
import com.moes.ui.screens.LocationPermissionScreen
import com.moes.ui.theme.MoesTheme
import com.moes.utils.NetworkMonitor

val LocalNetworkStatus = compositionLocalOf { true }

@Composable
fun MoesApp() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val networkMonitor = remember { NetworkMonitor(context) }
    val isOnline by networkMonitor.isOnline.collectAsState(initial = true)

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            hasLocationPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    MoesTheme {
        CompositionLocalProvider(LocalNetworkStatus provides isOnline) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
            ) {
                OfflineBanner(isOffline = !isOnline)

                Box(modifier = Modifier.weight(1f)) {
                    if (hasLocationPermission) {
                        MoesNavHost()
                    } else {
                        LocationPermissionScreen(
                            onRequestPermission = {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        )
                    }
                }
            }
        }
    }
}