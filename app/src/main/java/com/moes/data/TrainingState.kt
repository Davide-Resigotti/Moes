package com.moes.data

import kotlinx.serialization.Serializable

@Serializable
enum class TrainingState {
    IDLE,
    TRACKING,
    PAUSED
}