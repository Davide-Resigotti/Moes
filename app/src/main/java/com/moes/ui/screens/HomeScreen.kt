package com.moes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.moes.data.TrainingState
import com.moes.ui.viewmodels.HomeScreenViewModel
import com.moes.ui.viewmodels.ViewModelFactory

@Composable
fun HomeScreen(
    navController: NavHostController,
    // Use the factory to create the ViewModel
    viewModel: HomeScreenViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    // Collect the state from the ViewModel
    val trainingState by viewModel.trainingState.collectAsState()
    val liveDuration by viewModel.liveDuration.collectAsState()
    val liveDistance by viewModel.liveDistance.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "State: $trainingState")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Duration: ${liveDuration / 1000}s")
        Text(text = "Distance: ${liveDistance.toInt()}m")
        Spacer(modifier = Modifier.height(32.dp))

        // Show buttons based on the current training state
        when (trainingState) {
            TrainingState.IDLE -> {
                Button(onClick = { viewModel.onStartTraining() }) {
                    Text("Start Training")
                }
            }
            TrainingState.TRACKING -> {
                Button(onClick = { viewModel.onPauseTraining() }) {
                    Text("Pause Training")
                }
            }
            TrainingState.PAUSED -> {
                Row {
                    Button(onClick = { viewModel.onResumeTraining() }) {
                        Text("Resume")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { viewModel.onStopTraining() }) {
                        Text("Stop")
                    }
                }
            }
        }
    }
}
