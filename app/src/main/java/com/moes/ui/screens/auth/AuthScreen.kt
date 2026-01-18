package com.moes.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moes.ui.viewmodels.AuthViewModel
import com.moes.ui.viewmodels.ViewModelFactory

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    if (!viewModel.isAnonymous) {
        LaunchedEffect(Unit) {
            onLoginSuccess()
        }
    }

    var showRegister by remember { mutableStateOf(false) }

    if (showRegister) {
        RegisterScreen(
            viewModel = viewModel,
            onGoToLogin = { showRegister = false }
        )
    } else {
        LoginScreen(
            viewModel = viewModel,
            onGoToRegister = { showRegister = true }
        )
    }
}