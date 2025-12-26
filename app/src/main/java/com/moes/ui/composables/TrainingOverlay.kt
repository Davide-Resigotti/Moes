package com.moes.ui.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        color =  MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    label = "Time",
                    value = formatDurationShort(duration),
                    isLarge = false
                )

                StatItem(
                    label = "Avg. pace (/km)",
                    value = pace,
                    isLarge = true
                )

                StatItem(
                    label = "Distance (km)",
                    value = String.format("%.2f", distance / 1000),
                    isLarge = false
                )
            }

            // Buttons Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onStopClick) {
                    Icon(Icons.Default.Clear, contentDescription = "Stop Training")
                    Text("Stop")
                }

                Spacer(modifier = Modifier.width(16.dp))

                if (trainingState == TrainingState.RUNNING) {
                    Button(onClick = onPauseClick) {
                        Icon(Icons.Default.Face, contentDescription = "Pause Training")
                        Text("Pause")
                    }
                } else if (trainingState == TrainingState.PAUSED) {
                    Button(onClick = onResumeClick) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume Training")
                        Text("Resume")
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
                MaterialTheme.typography.displayMedium
            } else {
                MaterialTheme.typography.headlineMedium
            },
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}


@SuppressLint("DefaultLocale")
private fun formatDurationShort(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

    return if (hours > 0) {
        // Da 1 ora in poi: mostra HH:MM (ore:minuti)
        String.format("%02d:%02d", hours, minutes)
    } else {
        // Sotto 1 ora: mostra MM:SS (minuti:secondi)
        String.format("%02d:%02d", minutes, seconds)
    }
}

@SuppressLint("DefaultLocale")
private fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
