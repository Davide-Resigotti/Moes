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
import com.moes.ui.composables.auth.AuthForm
import com.moes.ui.composables.auth.GoogleLoginButton
import com.moes.ui.composables.auth.rememberGoogleLoginLauncher
import com.moes.ui.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(viewModel: AuthViewModel, onGoToLogin: () -> Unit) {
    val launchGoogleLogin = rememberGoogleLoginLauncher(viewModel)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            AuthForm(
                title = "Crea Account",
                buttonText = "Registrati",
                viewModel = viewModel,
                onSubmit = { viewModel.onRegisterClick() },
                footer = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // DIVIDER "OPPURE"
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

                        // TASTO GOOGLE
                        GoogleLoginButton(
                            text = "Registrati con Google",
                            onClick = { launchGoogleLogin() }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // LINK LOGIN
                        TextButton(onClick = onGoToLogin) {
                            Text(
                                "Hai già un account? ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Accedi",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }
    }
}