package com.moes.ui.composables.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
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
    val isCompleted = progress.isFullyCompleted
    val rawProgress = progress.progressFloat

    val currentLevelInfo = def.levels[progress.currentLevelIndex]

    val levelTitleText = currentLevelInfo.title

    val levelTitleColor = MaterialTheme.colorScheme.primary

    val currentFormattedValue = remember(progress.currentValue, def.type) {
        FormatUtils.formatMissionValue(def.type, progress.currentValue)
    }

    val descriptionText = if (isCompleted) {
        val lastThreshold = def.levels.last().threshold
        val targetFormatted = FormatUtils.formatMissionValue(def.type, lastThreshold)
        String.format(def.descriptionTemplate, targetFormatted)
    } else {
        val targetFormatted = FormatUtils.formatMissionValue(def.type, progress.currentLevelTarget)
        String.format(def.descriptionTemplate, targetFormatted)
    }

    val counterText = if (isCompleted) {
        currentFormattedValue
    } else {
        val targetText = FormatUtils.formatMissionValue(def.type, progress.currentLevelTarget)
        "$currentFormattedValue / $targetText"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(verticalAlignment = Alignment.Top) {
                // ICONA
                val backgroundModifier = if (isCompleted) {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(BrandSecondary, LogoGradientStart)
                        )
                    )
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .then(backgroundModifier),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = def.icon,
                        contentDescription = null,
                        tint = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // TESTI CENTRALI
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Titolo Missione
                        Text(
                            text = def.baseTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Badge Livello
                        LevelBadge(
                            levelIndex = progress.currentLevelIndex,
                            totalLevels = def.levels.size,
                            isCompleted = isCompleted
                        )
                    }

                    // Nome del Livello
                    Text(
                        text = levelTitleText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = levelTitleColor
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Descrizione o Record
                    Text(
                        text = descriptionText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BARRA DI PROGRESSO
            val visualProgress = if (isCompleted) 1f else rawProgress.coerceIn(0.02f, 1f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Parte Piena
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

                // Parte Vuota (se c'è)
                if (visualProgress < 1f) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f - visualProgress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CONTATORE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = counterText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LevelBadge(
    levelIndex: Int,
    totalLevels: Int,
    isCompleted: Boolean
) {
    val displayText = if (isCompleted) {
        "MAX"
    } else {
        "LVL ${levelIndex + 1}/$totalLevels"
    }

    val backgroundColor = if (isCompleted) {
        BrandSecondary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isCompleted) {
        BrandSecondary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}