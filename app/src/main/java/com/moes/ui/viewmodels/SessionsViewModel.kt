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

@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModel(
    private val authRepository: AuthRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    // 1. Usiamo un Flow per tenere traccia dell'ID corrente dinamicamente
    private val _currentUserId = MutableStateFlow(authRepository.currentUserIdSafe)

    init {
        // 2. Ascoltiamo i cambiamenti di stato (Login/Logout)
        authRepository.addAuthStateListener { _ ->
            // Ogni volta che lo stato cambia, aggiorniamo l'ID
            _currentUserId.value = authRepository.currentUserIdSafe
        }
    }

    // 3. FlatMapLatest: ogni volta che _currentUserId cambia,
    // annulla la vecchia query e ne lancia una nuova con il nuovo ID.
    val sessions = _currentUserId.flatMapLatest { userId ->
        databaseRepository.getSessionsForUser(userId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}