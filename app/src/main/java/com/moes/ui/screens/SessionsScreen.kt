package com.moes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moes.ui.composables.sessions.SessionPillCard
import com.moes.ui.viewmodels.SessionsViewModel
import com.moes.ui.viewmodels.ViewModelFactory

@Composable
fun SessionsScreen(
    viewModel: SessionsViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onSessionClick: (String) -> Unit
) {
    val sessions by viewModel.sessions.collectAsState()

    val systemBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val listBottomPadding = 100.dp + systemBottomPadding

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Nessun allenamento registrato",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 16.dp,
                    start = 20.dp,
                    end = 20.dp,
                    bottom = listBottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // TITOLO
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "I tuoi allenamenti",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp, start = 4.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(sessions) { session ->
                    SessionPillCard(
                        session = session,
                        onClick = { onSessionClick(session.id) }
                    )
                }
            }
        }
    }
}