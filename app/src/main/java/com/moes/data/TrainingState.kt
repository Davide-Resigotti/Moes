package com.moes.data

import kotlinx.serialization.Serializable

@Serializable
enum class TrainingState {
    IDLE, // waiting for another training request
    RUNNING, // training is running
    PAUSED, // training is paused
}