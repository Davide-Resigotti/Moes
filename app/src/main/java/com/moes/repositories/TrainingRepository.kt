package com.moes.repositories

import android.content.Context
import android.content.Intent
import com.moes.data.TrainingState
import com.moes.data.live.LiveTrainingSession
import com.moes.data.live.toTrainingSession
import com.moes.services.LiveTrainingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Repository that manages the lifecycle of a training session.
 * It is the single source of truth for the training state and data.
 */
class TrainingRepository(
    context: Context,
    private val databaseRepository: DatabaseRepository,
    private val authRepository: AuthRepository,
    externalScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val appContext = context.applicationContext

    // Expose the training state directly from the service.
    val trainingState: StateFlow<TrainingState> = LiveTrainingService.trainingState
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), TrainingState.IDLE)

    val isTracking: StateFlow<Boolean> = trainingState.map { it == TrainingState.RUNNING }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), false)

    val isPaused: StateFlow<Boolean> = trainingState.map { it == TrainingState.PAUSED }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), false)

    val liveTrainingSession: StateFlow<LiveTrainingSession?> =
        LiveTrainingService.liveTrainingSession
            .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), null)

    private val _finishedSessionId = MutableStateFlow<String?>(null)
    val finishedSessionId: StateFlow<String?> = _finishedSessionId

    init {
        externalScope.launch {
            LiveTrainingService.completedTrainingSession.collect { liveSession ->
                liveSession?.let { session ->
                    saveFinishedSession(session)
                }
            }
        }
    }

    fun startTracking() {
        val intent = Intent(appContext, LiveTrainingService::class.java).apply {
            action = LiveTrainingService.ACTION_START
        }
        appContext.startService(intent)
    }

    fun pauseTracking() {
        val intent = Intent(appContext, LiveTrainingService::class.java).apply {
            action = LiveTrainingService.ACTION_PAUSE
        }
        appContext.startService(intent)
    }

    fun resumeTracking() {
        val intent = Intent(appContext, LiveTrainingService::class.java).apply {
            action = LiveTrainingService.ACTION_RESUME
        }
        appContext.startService(intent)
    }

    fun stopTracking() {
        val intent = Intent(appContext, LiveTrainingService::class.java).apply {
            action = LiveTrainingService.ACTION_STOP
        }
        appContext.startService(intent)
    }


    private suspend fun saveFinishedSession(liveSession: LiveTrainingSession) {
        val userId = authRepository.currentUserIdSafe

        // Assegniamo un nome di default basato sull'ora (o generico)
        val defaultTitle = "Corsa ${
            java.text.SimpleDateFormat("dd/MM HH:mm").format(java.util.Date(liveSession.startTime))
        }"

        // Mapperemo includendo il titolo (devi aggiornare il mapper in LiveTrainingSession.kt o passare il titolo qui)
        // Per semplicità, assumiamo che il mapper ora accetti il titolo o lo mettiamo di default nel costruttore Entity
        val session = liveSession.toTrainingSession(userId).copy(title = defaultTitle)

        databaseRepository.saveTrainingSession(session)

        // Notifichiamo la UI che abbiamo salvato questo ID
        _finishedSessionId.value = session.id
    }

    // Metodo per resettare l'evento di navigazione
    fun clearFinishedSessionEvent() {
        _finishedSessionId.value = null
    }
}

