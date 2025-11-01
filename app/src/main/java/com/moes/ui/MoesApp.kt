package com.moes.ui

import androidx.compose.runtime.Composable
import com.moes.ui.navigation.MoesNavHost
import com.moes.ui.theme.MoesTheme

@Composable
fun MoesApp() {
    MoesTheme {
        MoesNavHost()
    }
}