package com.moes.ui.composables.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moes.ui.theme.LogoGradientEnd
import com.moes.ui.theme.LogoGradientStart

@Composable
fun ActivityIcon(speedKmh: Double) {
    val (icon, description) = when {
        speedKmh < 6.5 -> Icons.AutoMirrored.Filled.DirectionsWalk to "Camminata"
        speedKmh < 20.0 -> Icons.AutoMirrored.Filled.DirectionsRun to "Corsa"
        else -> Icons.AutoMirrored.Filled.DirectionsBike to "Ciclismo"
    }

    Box(
        modifier = Modifier.Companion
            .size(56.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.Companion.linearGradient(
                    colors = listOf(LogoGradientStart, LogoGradientEnd)
                )
            ), contentAlignment = Alignment.Companion.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.Companion.White,
            modifier = Modifier.Companion.size(28.dp)
        )
    }
}