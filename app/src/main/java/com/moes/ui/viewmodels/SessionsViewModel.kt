package com.moes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moes.repositories.AuthRepository
import com.moes.repositories.DatabaseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModel(
    private val authRepository: AuthRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val _currentUserId = MutableStateFlow(authRepository.currentUserIdSafe)

    init {
        authRepository.addAuthStateListener { _ ->
            _currentUserId.value = authRepository.currentUserIdSafe
        }

        refreshData()
    }

    val sessions = _currentUserId.flatMapLatest { userId ->
        databaseRepository.getSessionsForUser(userId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun refreshData() {
        viewModelScope.launch {
            val userId = authRepository.currentUserIdSafe
            if (userId != AuthRepository.GUEST_ID) {
                databaseRepository.syncPendingSessions()
                databaseRepository.syncFromCloud(userId)
            }
        }
    }
}