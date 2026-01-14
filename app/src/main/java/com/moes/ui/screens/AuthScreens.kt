package com.moes.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moes.ui.viewmodels.AuthViewModel
import com.moes.ui.viewmodels.ViewModelFactory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation

// 1. AUTH SCREEN (Il Container Principale)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    if (viewModel.isAnonymous) {
        // Se anonimo, gestiamo la navigazione interna tra Login e Register
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
    } else {
        // Se loggato ufficialmente, mostriamo il profilo
        AccountScreen(onLogout = { viewModel.logout() })
    }
}

// 2. LOGIN SCREEN
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

// 3. REGISTER SCREEN
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

// COMPONENTE COMUNE PER IL FORM
@Composable
fun AuthForm(
    title: String,
    buttonText: String,
    viewModel: AuthViewModel,
    onSubmit: () -> Unit,
    footer: @Composable () -> Unit
) {
    // Stato per la visibilità della password
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        // CAMPO EMAIL
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true, // EVITA L'ANDATA A CAPO
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next // Il tasto invio porta alla password
            )
        )
        Spacer(Modifier.height(8.dp))

        // CAMPO PASSWORD
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true, // EVITA L'ANDATA A CAPO
            // Gestione visibilità: se true mostra testo normale, se false mostra pallini
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done // Il tasto invio chiude la tastiera
            ),
            trailingIcon = {
                // Icona Occhio
                val image = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

                // Usiamo Icon invece di IconButton per gestire il "tieni premuto" personalizzato
                Icon(
                    imageVector = image,
                    contentDescription = "Tieni premuto per mostrare la password",
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPasswordVisible = true // Mostra quando premi
                                    tryAwaitRelease()        // Aspetta che l'utente alzi il dito
                                    isPasswordVisible = false // Nascondi quando rilascia
                                }
                            )
                        }
                        .padding(8.dp) // Un po' di padding per rendere il tocco più facile
                )
            }
        )

        if (viewModel.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(viewModel.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading // Disabilita se sta caricando
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(buttonText)
            }
        }

        Spacer(Modifier.height(16.dp))
        footer()
    }
}