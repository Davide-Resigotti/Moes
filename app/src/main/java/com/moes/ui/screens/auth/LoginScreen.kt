package com.moes.ui.screens.auth

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.moes.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(viewModel: AuthViewModel, onGoToRegister: () -> Unit) {
    AuthForm(
        title = "Accedi",
        buttonText = "Login",
        viewModel = viewModel,
        onSubmit = { viewModel.onLoginClick() },
        footer = {
            TextButton(onClick = onGoToRegister) {
                Text("Non hai un account? Registrati (Mantieni i dati)")
            }
        }
    )
}