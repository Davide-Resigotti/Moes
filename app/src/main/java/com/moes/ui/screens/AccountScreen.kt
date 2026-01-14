package com.moes.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccountScreen(onLogout: () -> Unit = {}) { // Parametro opzionale per compatibilità
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Profilo Utente", style = MaterialTheme.typography.headlineMedium)
            Text("Sei loggato e i tuoi dati sono al sicuro.")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onLogout) {
                Text("Logout")
            }
        }
    }
}
