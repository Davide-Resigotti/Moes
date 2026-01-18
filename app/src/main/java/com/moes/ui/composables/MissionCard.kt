package com.moes.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moes.data.missions.MissionProgress
import com.moes.ui.theme.BrandSecondary
import com.moes.ui.theme.LogoGradientEnd
import com.moes.ui.theme.LogoGradientStart
import com.moes.utils.FormatUtils

@Composable
fun MissionCard(progress: MissionProgress) {
    val def = progress.definition
    val isCompleted = progress.isCompleted
    val rawProgress = progress.progressFloat

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- ICONA MISSIONE ---
            val backgroundModifier = if (isCompleted) {
                Modifier.background(
                    brush = Brush.linearGradient(
                        colors = listOf(LogoGradientStart, LogoGradientEnd)
                    )
                )
            } else {
                Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .then(backgroundModifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = def.icon,
                    contentDescription = null,
                    tint = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                // TITOLO
                Text(
                    text = def.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // DESCRIZIONE
                Text(
                    text = def.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // --- PROGRESS BAR
                val visualProgress = if (rawProgress > 0f && rawProgress < 1f) {
                    rawProgress.coerceIn(0.05f, 0.95f)
                } else {
                    rawProgress
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (rawProgress > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(visualProgress)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    brush = if (isCompleted) {
                                        Brush.linearGradient(listOf(BrandSecondary, BrandSecondary))
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(LogoGradientStart, LogoGradientEnd)
                                        )
                                    }
                                )
                        )
                    }

                    if (rawProgress > 0f && rawProgress < 1f) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    if (rawProgress < 1f) {
                        Box(
                            modifier = Modifier
                                .weight(1f - visualProgress)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // TESTO PROGRESSO
                val currentText = FormatUtils.formatMissionValue(def.type, progress.currentValue)
                val targetText = FormatUtils.formatMissionValue(def.type, def.threshold)

                Text(
                    text = if (isCompleted) "Completata!" else "$currentText / $targetText",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isCompleted) BrandSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}