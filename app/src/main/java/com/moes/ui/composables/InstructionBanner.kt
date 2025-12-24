package com.moes.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun InstructionBanner(
    instruction: String,
    distanceRemaining: String,
    maneuverType: String?,
    maneuverModifier: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ManeuverIcon(maneuverType, maneuverModifier)

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Text Content
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                // Distance (e.g., "500m")
                Text(
                    text = distanceRemaining,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                // Instruction (e.g., "Turn right onto Main St")
                Text(
                    text = instruction,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


@Composable
private fun ManeuverIcon(maneuverType: String?, maneuverModifier: String?) {
    val (icon, rotation) = getManeuverIcon(maneuverType, maneuverModifier)

    Icon(
        imageVector = icon,
        contentDescription = "Direction",
        modifier = Modifier
            .size(48.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .rotate(rotation),
        tint = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

private fun getManeuverIcon(type: String?, modifier: String?): Pair<ImageVector, Float> {
    val baseIcon = Icons.AutoMirrored.Filled.ArrowForward
    return when (type) {
        "arrive" -> baseIcon to 0f // Placeholder for arrival
        "depart" -> baseIcon to 0f // Placeholder for departure
        "turn", "fork", "roundabout", "rotary" -> {
            when (modifier) {
                "sharp right" -> baseIcon to 45f
                "right" -> baseIcon to 0f
                "slight right" -> baseIcon to -45f
                "straight" -> baseIcon to -90f
                "slight left" -> baseIcon to -135f
                "left" -> baseIcon to 180f
                "sharp left" -> baseIcon to 135f
                "uturn" -> baseIcon to 90f
                else -> baseIcon to -90f // Default for turns is straight
            }
        }

        else -> baseIcon to -90f // Default for unknown types is straight
    }
}
