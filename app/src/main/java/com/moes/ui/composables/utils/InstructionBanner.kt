package com.moes.ui.composables.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moes.utils.FormatUtils

@Composable
fun InstructionBanner(
    instruction: String,
    distanceRemaining: String,
    maneuverType: String?,
    maneuverModifier: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = CircleShape,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ManeuverIcon(maneuverType, maneuverModifier)

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = FormatUtils.formatRawDistance(distanceRemaining),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                if (instruction.isNotEmpty()) {
                    Text(
                        text = instruction,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ManeuverIcon(maneuverType: String?, maneuverModifier: String?) {
    val (icon, rotation) = getManeuverIcon(maneuverType, maneuverModifier)

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Direction",
            modifier = Modifier
                .size(24.dp)
                .rotate(rotation),
            tint = Color.White
        )
    }
}

private fun getManeuverIcon(type: String?, modifier: String?): Pair<ImageVector, Float> {
    val baseIcon = Icons.AutoMirrored.Filled.ArrowForward
    return when (type) {
        "arrive", "depart" -> baseIcon to -90f
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
                else -> baseIcon to -90f
            }
        }

        else -> baseIcon to -90f
    }
}