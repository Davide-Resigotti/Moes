package com.moes.ui.screens.auth

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.moes.ui.composables.AuthForm
import com.moes.ui.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(viewModel: AuthViewModel, onGoToLogin: () -> Unit) {
    AuthForm(
        title = "Crea Account",
        buttonText = "Registrati e Salva Dati",
        viewModel = viewModel,
        onSubmit = { viewModel.onRegisterClick() },
        footer = {
            TextButton(onClick = onGoToLogin) {
                Text("Hai già un account? Accedi")
            }
        }
    )
}