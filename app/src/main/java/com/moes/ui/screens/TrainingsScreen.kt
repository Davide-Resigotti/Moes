package com.moes.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TrainingsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("This is the Trainings Screen content!")
        // Further content for the fully expanded sheet can go here
    }
}
