package com.moes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moes.ui.composables.SessionRouteMap
import com.moes.ui.viewmodels.SessionDetailViewModel
import com.moes.ui.viewmodels.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ... imports rimangono uguali

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: String,
    // MODIFICA: Rimosso parametro isPostWorkout
    onNavigateBack: () -> Unit,
    viewModel: SessionDetailViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    // ... (LaunchedEffect e caricamento sessione rimangono uguali) ...
    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    val session by viewModel.session.collectAsState()

    var titleText by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(session) {
        if (!isInitialized) {
            session?.let { s ->
                titleText = s.title
                isInitialized = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                // MODIFICA: Titolo unificato
                title = { Text("Dettaglio Allenamento") },
                navigationIcon = {
                    // MODIFICA: Icona indietro sempre visibile
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        // ... (Tutto il contenuto del corpo rimane identico) ...
        if (session == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val s = session!!
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // ... Mappa, TextField, Statistiche e Bottoni rimangono uguali ...
                // COPIA INCOLLA IL RESTO DEL CONTENUTO CHE AVEVI GIA'
                // (Mappa, Titolo editabile, Data, StatBox, Bottoni Elimina/Salva)

                // 1. MAPPA
                Box(modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()) {
                    SessionRouteMap(
                        encodedGeometry = s.routeGeometry,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 2. CONTENUTO
                Column(modifier = Modifier.padding(16.dp)) {

                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("Nome Allenamento") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(
                            Date(s.startTime)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(24.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatBox("Distanza", String.format("%.2f km", s.distanceMeters / 1000))
                        StatBox("Durata", formatDuration(s.durationMs))
                        StatBox("Ritmo", calculatePace(s.durationMs, s.distanceMeters))
                    }

                    Spacer(Modifier.height(32.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.deleteSession { onNavigateBack() }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Elimina")
                        }

                        Button(
                            onClick = {
                                viewModel.saveTitle(titleText) { onNavigateBack() }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Salva")
                        }
                    }
                }
            }
        }
    }
}

// ... Funzioni helper (StatBox, formatDuration, calculatePace) rimangono uguali
@Composable
fun StatBox(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.headlineSmall)
    }
}

private fun formatDuration(millis: Long): String {
    val minutes = (millis / 1000) / 60
    val seconds = (millis / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

private fun calculatePace(durationMs: Long, distanceMeters: Double): String {
    if (distanceMeters == 0.0) return "0:00"
    val secondsPerKm = (durationMs / 1000.0) / (distanceMeters / 1000.0)
    val min = (secondsPerKm / 60).toInt()
    val sec = (secondsPerKm % 60).toInt()
    return String.format("%d:%02d /km", min, sec)
}