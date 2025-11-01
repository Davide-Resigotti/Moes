package com.moes.repositories

import com.moes.data.TrainingSession

/**
 * A repository responsible for saving and retrieving training data from a database.
 */
interface DatabaseRepository {
    /**
     * Saves a completed training session to the database.
     */
    suspend fun saveTrainingSession(session: TrainingSession)
}
