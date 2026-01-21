package com.moes.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.statusBarsPaddingIfOnline(isOnline: Boolean): Modifier {
    return if (isOnline) this.statusBarsPadding() else this
}

fun Modifier.topPaddingDynamic(
    isOnline: Boolean,
    onlinePadding: Dp,
    offlinePadding: Dp = 0.dp
): Modifier {
    return this.padding(top = if (isOnline) onlinePadding else offlinePadding)
}