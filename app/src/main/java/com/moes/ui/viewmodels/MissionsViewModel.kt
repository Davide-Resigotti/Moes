package com.moes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moes.data.missions.MissionProgress
import com.moes.repositories.AuthRepository
import com.moes.repositories.GamificationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class MissionsViewModel(
    private val gamificationRepository: GamificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _currentUserId = MutableStateFlow(authRepository.currentUserIdSafe)

    init {
        authRepository.addAuthStateListener {
            _currentUserId.value = authRepository.currentUserIdSafe
        }
    }

    val missions: StateFlow<List<MissionProgress>> = _currentUserId.flatMapLatest { userId ->
        gamificationRepository.getMissionsProgress(userId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}