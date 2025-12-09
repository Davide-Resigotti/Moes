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
import androidx.compose.ui.unit.dp
import com.moes.data.TrainingState
import java.util.concurrent.TimeUnit

@SuppressLint("DefaultLocale")
@Composable
fun TrainingOverlay(
    trainingState: TrainingState,
    duration: Long,
    distance: Double,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Row
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                StatItem(label = "Duration", value = formatDuration(duration))
                StatItem(
                    label = "Distance (km)",
                    value = String.format("%.2f", distance / 1000)
                )
            }

            // Buttons Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onStopClick,
                ) {
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
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.displaySmall)
    }
}

@SuppressLint("DefaultLocale")
private fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
