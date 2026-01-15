package com.moes.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moes.data.TrainingSession
import com.moes.ui.viewmodels.SessionsViewModel
import com.moes.ui.viewmodels.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionsScreen(
    viewModel: SessionsViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    // 1. Aggiungi questo parametro per gestire il click
    onSessionClick: (String) -> Unit
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
                SessionCard(
                    session = session,
                    // 2. Passa l'evento di click
                    onClick = { onSessionClick(session.id) }
                )
            }
        }
    }
}

@Composable
fun SessionCard(
    session: TrainingSession,
    onClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // RIGA 1: Titolo e Icona Sync
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Titolo (prende tutto lo spazio disponibile meno l'icona)
                Text(
                    text = session.title, // Usa il titolo personalizzato!
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Icona Sync
                Icon(
                    imageVector = if (session.isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = if (session.isSynced) "Sincronizzato" else "Da sincronizzare",
                    tint = if (session.isSynced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }

            // RIGA 2: Data (Sottotitolo)
            Text(
                text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(session.startTime)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Colore grigio/secondario
            )

            Spacer(modifier = Modifier.height(12.dp))

            // RIGA 3: Statistiche (Distanza e Durata)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Distanza
                Column {
                    Text(
                        text = "Distanza",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.2f", session.distanceMeters / 1000)} km",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Durata
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Durata",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDuration(session.durationMs),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
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