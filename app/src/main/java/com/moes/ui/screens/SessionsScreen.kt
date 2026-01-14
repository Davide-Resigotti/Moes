package com.moes.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moes.data.TrainingSession
import com.moes.ui.viewmodels.SessionsViewModel
import com.moes.ui.viewmodels.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SessionsScreen(
    // Ora usa la factory automaticamente per recuperare il ViewModel vero
    viewModel: SessionsViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val sessions by viewModel.sessions.collectAsState()

    if (sessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nessun allenamento registrato")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(sessions) { session ->
                SessionCard(session)
            }
        }
    }
}

@Composable
fun SessionCard(session: TrainingSession) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Riga superiore: Data e Icona Sync
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(session.startTime)),
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (session.isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = if (session.isSynced) "Sincronizzato" else "Da sincronizzare",
                    tint = if (session.isSynced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Riga inferiore: Distanza e Durata
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${String.format("%.2f", session.distanceMeters / 1000)} km")
                Text(formatDuration(session.durationMs))
            }
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatDuration(millis: Long): String {
    val minutes = (millis / 1000) / 60
    val seconds = (millis / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}