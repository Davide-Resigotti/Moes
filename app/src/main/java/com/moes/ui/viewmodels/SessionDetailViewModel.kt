package com.moes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moes.data.TrainingSession
import com.moes.data.UserProfile
import com.moes.repositories.DatabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SessionDetailViewModel(
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val _session = MutableStateFlow<TrainingSession?>(null)
    val session: StateFlow<TrainingSession?> = _session.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            databaseRepository.getSessionByIdFlow(sessionId).collectLatest { result ->
                _session.value = result

                if (result != null && _userProfile.value == null) {
                    launch {
                        databaseRepository.getUserProfile(result.userId).collect { profile ->
                            _userProfile.value = profile
                        }
                    }
                }
            }
        }
    }

    fun saveTitle(newTitle: String) {
        val currentSession = _session.value ?: return
        val trimmedTitle = newTitle.trim()

        if (trimmedTitle.isBlank() || currentSession.title == trimmedTitle) return

        viewModelScope.launch {
            try {
                databaseRepository.updateSessionTitle(currentSession.id, trimmedTitle)
                databaseRepository.syncPendingSessions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSession(onComplete: () -> Unit) {
        val currentSessionId = _session.value?.id ?: return

        viewModelScope.launch {
            try {
                databaseRepository.deleteSession(currentSessionId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onComplete()
            }
        }
    }
}