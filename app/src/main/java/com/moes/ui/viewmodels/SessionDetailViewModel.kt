package com.moes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moes.data.TrainingSession
import com.moes.repositories.DatabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionDetailViewModel(
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val _session = MutableStateFlow<TrainingSession?>(null)
    val session: StateFlow<TrainingSession?> = _session.asStateFlow()

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            val result = databaseRepository.getSessionById(sessionId)
            _session.value = result
        }
    }

    fun saveTitle(newTitle: String, onComplete: () -> Unit) {
        val currentSession = _session.value ?: return
        val trimmedTitle = newTitle.trim()

        if (currentSession.title == trimmedTitle) {
            onComplete()
            return
        }

        viewModelScope.launch {
            try {
                databaseRepository.updateSessionTitle(currentSession.id, trimmedTitle)
                _session.value = currentSession.copy(title = trimmedTitle)
                databaseRepository.syncPendingSessions()
            } catch (_: Exception) {
            } finally {
                // Assicura che la navigazione avvenga comunque
                onComplete()
            }
        }
    }

    fun deleteSession(onComplete: () -> Unit) {
        // Usiamo l'ID della sessione corrente, o usciamo se non c'è (caso raro)
        val currentSessionId = _session.value?.id ?: return

        viewModelScope.launch {
            try {
                databaseRepository.deleteSession(currentSessionId)
            } catch (_: Exception) {
            } finally {
                // FONDAMENTALE: Torna indietro SEMPRE, anche se c'è stato un errore o il DB era lento.
                // Questo garantisce che l'utente non rimanga bloccato sulla schermata.
                onComplete()
            }
        }
    }
}