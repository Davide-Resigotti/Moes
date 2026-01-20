package com.moes.ui.composables.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moes.data.UserProfile
import com.moes.ui.theme.LogoGradientEnd
import com.moes.ui.theme.LogoGradientStart
import java.util.Calendar

@Composable
fun AccountHeader(
    profile: UserProfile, streak: Int, lastTrainingDate: Long, isGuest: Boolean
) {
    val initials = remember(profile.firstName, profile.lastName) {
        val first = profile.firstName.take(1)
        val last = profile.lastName.take(1)
        if (first.isBlank() && last.isBlank()) "U" else (first + last).uppercase()
    }

    val displayName = "${profile.firstName} ${profile.lastName}".trim()

    val (showStreak, streakEmoji) = remember(streak, lastTrainingDate) {
        if (streak <= 0) return@remember false to ""
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)
        val todayYear = cal.get(Calendar.YEAR)

        cal.timeInMillis = lastTrainingDate
        val lastDay = cal.get(Calendar.DAY_OF_YEAR)
        val lastYear = cal.get(Calendar.YEAR)

        val isTrainedToday = (todayDay == lastDay && todayYear == lastYear)

        if (isTrainedToday) {
            when {
                streak >= 100 -> true to "💯"
                streak >= 10 -> true to "🥳"
                else -> true to "🔥"
            }
        } else {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val hoursLeft = 24 - currentHour
            when {
                hoursLeft <= 2 -> true to "⌛"
                hoursLeft <= 5 -> true to "⏳"
                streak >= 100 -> true to "💯"
                streak >= 10 -> true to "🥳"
                else -> true to "🔥"
            }
        }
    }

    Surface(
        modifier = Modifier.Companion.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.Companion.padding(20.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            // AVATAR
            Box(contentAlignment = Alignment.Companion.BottomCenter) {
                // Avatar
                Box(
                    modifier = Modifier.Companion
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.Companion.linearGradient(
                                colors = listOf(LogoGradientStart, LogoGradientEnd)
                            )
                        ), contentAlignment = Alignment.Companion.Center
                ) {
                    Text(
                        text = initials, style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Companion.Bold, letterSpacing = 1.sp
                        ), color = Color.Companion.White
                    )
                }

                // Badge Streak
                if (showStreak) {
                    Box(
                        modifier = Modifier.Companion
                            .offset(y = 8.dp)
                            .shadow(4.dp, CircleShape)
                            .border(
                                2.dp, MaterialTheme.colorScheme.surface, CircleShape
                            )
                            .clip(CircleShape)
                            .background(
                                brush = Brush.Companion.linearGradient(
                                    colors = listOf(LogoGradientStart, LogoGradientEnd)
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                            Text(text = streakEmoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.Companion.width(4.dp))
                            Text(
                                text = "$streak", style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Companion.Bold, fontSize = 12.sp
                                ), color = Color.Companion.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.Companion.width(20.dp))

            // NOME E INFO
            Column(modifier = Modifier.Companion.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Companion.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.Companion.height(4.dp))

                // Stato Sync
                Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                    Icon(
                        imageVector = if (isGuest) Icons.Default.CloudOff else Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = if (isGuest) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.Companion.size(14.dp)
                    )
                    Spacer(modifier = Modifier.Companion.width(4.dp))
                    Text(
                        text = if (isGuest) "Non sincronizzato" else "Sincronizzato",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isGuest) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}