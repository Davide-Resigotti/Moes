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

    // Aggiorna il titolo e salva nel DB
    fun saveTitle(newTitle: String, onComplete: () -> Unit) {
        val currentSession = _session.value ?: return

        // 1. TRIM: Rimuove spazi vuoti all'inizio e alla fine
        val trimmedTitle = newTitle.trim()

        // Se il titolo non è cambiato (dopo il trim), non fare nulla ed esci
        if (currentSession.title == trimmedTitle) {
            onComplete()
            return
        }

        viewModelScope.launch {
            // 2. Aggiorna DB locale con il titolo pulito
            databaseRepository.updateSessionTitle(currentSession.id, trimmedTitle)

            // 3. Aggiorna lo stato UI locale
            _session.value = currentSession.copy(title = trimmedTitle)

            // 4. Tenta subito di sincronizzare con Firebase
            databaseRepository.syncPendingSessions()

            onComplete()
        }
    }

    fun deleteSession(onComplete: () -> Unit) {
        val currentSession = _session.value ?: return
        viewModelScope.launch {
            databaseRepository.deleteSession(currentSession.id)
            onComplete()
        }
    }
}