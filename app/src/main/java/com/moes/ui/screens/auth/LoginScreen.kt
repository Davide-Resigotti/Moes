package com.moes.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moes.ui.composables.AuthForm
import com.moes.ui.composables.GoogleLoginButton
import com.moes.ui.composables.rememberGoogleLoginLauncher
import com.moes.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(viewModel: AuthViewModel, onGoToRegister: () -> Unit) {
    val launchGoogleLogin = rememberGoogleLoginLauncher(viewModel)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            AuthForm(
                title = "Bentornato!",
                buttonText = "Accedi",
                viewModel = viewModel,
                onSubmit = { viewModel.onLoginClick() },
                footer = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Divider "oppure"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            Text(
                                text = "oppure",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }

                        // GOOGLE LOGIN BUTTON
                        GoogleLoginButton(
                            onClick = { launchGoogleLogin() })

                        Spacer(modifier = Modifier.height(8.dp))

                        // Link Registrazione
                        TextButton(onClick = onGoToRegister) {
                            Text(
                                "Non hai un account? ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Registrati",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                })
        }
    }
}