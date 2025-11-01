package com.moes.repositories

import com.moes.data.TrainingSession

/**
 * A simple implementation of the DatabaseRepository that prints the saved session to the console.
 * This is useful for debugging purposes.
 */
class DebugDatabaseRepository : DatabaseRepository {
    override suspend fun saveTrainingSession(session: TrainingSession) {
        println("Saving session: $session")
    }
}
