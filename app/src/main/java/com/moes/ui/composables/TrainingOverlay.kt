package com.moes.ui.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moes.data.TrainingState
import java.util.concurrent.TimeUnit

@SuppressLint("DefaultLocale")
@Composable
fun TrainingOverlay(
    trainingState: TrainingState,
    duration: Long,
    distance: Double,
    pace: String,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp), // Aumentato per effetto "Pillola" coerente con la Navbar
        shadowElevation = 12.dp, // Stessa ombra della Navbar
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // RIGA STATISTICHE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    label = "Tempo",
                    value = formatDurationShort(duration),
                    isLarge = false
                )

                StatItem(
                    label = "Passo (/km)",
                    value = pace,
                    isLarge = true
                )

                StatItem(
                    label = "Km",
                    value = String.format("%.2f", distance / 1000),
                    isLarge = false
                )
            }

            // RIGA PULSANTI
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Tasto STOP (Rosso/Errore)
                Button(
                    onClick = onStopClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Stop")
                    Spacer(Modifier.width(8.dp))
                    Text("Stop")
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Tasto PAUSE/RESUME (Arancione/Primary)
                if (trainingState == TrainingState.RUNNING) {
                    Button(onClick = onPauseClick) {
                        Icon(Icons.Default.Face, contentDescription = "Pause")
                        Spacer(Modifier.width(8.dp))
                        Text("Pausa")
                    }
                } else if (trainingState == TrainingState.PAUSED) {
                    Button(onClick = onResumeClick) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                        Spacer(Modifier.width(8.dp))
                        Text("Riprendi")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, isLarge: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = if (isLarge) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@SuppressLint("DefaultLocale")
private fun formatDurationShort(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

    return if (hours > 0) {
        String.format("%02d:%02d", hours, minutes)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}