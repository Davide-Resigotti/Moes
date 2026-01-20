package com.moes.ui.composables.utils

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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moes.data.TrainingState
import com.moes.utils.FormatUtils

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
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 12.dp,
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
                StatItem("Tempo", FormatUtils.formatDuration(duration), false)
                StatItem("Passo (/km)", pace, true)
                StatItem("Km", String.format("%.2f", distance / 1000), false)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // STOP
                Button(
                    onClick = onStopClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                    Spacer(Modifier.width(8.dp))
                    Text("Stop")
                }

                Spacer(modifier = Modifier.width(16.dp))

                // PAUSE/RESUME
                val actionButtonColors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )

                if (trainingState == TrainingState.RUNNING) {
                    Button(onClick = onPauseClick, colors = actionButtonColors) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause")
                        Spacer(Modifier.width(8.dp))
                        Text("Pausa")
                    }
                } else if (trainingState == TrainingState.PAUSED) {
                    Button(onClick = onResumeClick, colors = actionButtonColors) {
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
            style = if (isLarge) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}