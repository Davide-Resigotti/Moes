package com.moes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moes.data.missions.MissionProgress
import com.moes.repositories.GamificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MissionsViewModel(
    gamificationRepository: GamificationRepository
) : ViewModel() {

    val missions: StateFlow<List<MissionProgress>> = gamificationRepository.getMissionsProgress()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}