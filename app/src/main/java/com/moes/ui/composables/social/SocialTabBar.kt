package com.moes.ui.composables.social

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SocialTabBar(
    selectedTab: Int, onTabSelected: (Int) -> Unit, requestCount: Int
) {
    val items = listOf("Amici" to Icons.Default.People, "Richieste" to Icons.Default.Inbox)

    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .height(56.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val itemWidth = maxWidth / items.size

            val indicatorOffset by animateDpAsState(
                targetValue = itemWidth * selectedTab,
                animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.7f),
                label = "indicator"
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            )

            Row(modifier = Modifier.fillMaxSize()) {
                items.forEachIndexed { index, (title, icon) ->
                    val isSelected = selectedTab == index

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "color"
                    )

                    Box(
                        modifier = Modifier
                            .width(itemWidth)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(index) }, contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = contentColor
                            )

                            if (index == 1 && requestCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(MaterialTheme.colorScheme.error, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}