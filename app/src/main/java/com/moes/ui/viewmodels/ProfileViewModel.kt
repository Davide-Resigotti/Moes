package com.moes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moes.data.UserProfile
import com.moes.repositories.AuthRepository
import com.moes.repositories.DatabaseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val currentUserId = MutableStateFlow(authRepository.currentUserIdSafe)

    init {
        authRepository.addAuthStateListener {
            currentUserId.value = authRepository.currentUserIdSafe
        }
    }

    val isGuest: StateFlow<Boolean> = currentUserId.map { userId ->
        userId == AuthRepository.GUEST_ID
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val userProfile: StateFlow<UserProfile> = currentUserId.flatMapLatest { userId ->
        databaseRepository.getUserProfile(userId).map { profile ->
            profile ?: UserProfile(userId = userId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile(userId = authRepository.currentUserIdSafe)
    )

    fun saveProfile(
        firstName: String,
        lastName: String,
        weight: String,
        height: String,
        gender: String
    ) {
        viewModelScope.launch {
            val w = weight.toFloatOrNull() ?: 0f
            val h = height.toFloatOrNull() ?: 0f
            val activeUserId = currentUserId.value
            val currentProfile = userProfile.value

            val updatedProfile = currentProfile.copy(
                userId = activeUserId,
                firstName = firstName,
                lastName = lastName,
                weightKg = w,
                heightCm = h,
                gender = gender,
                lastEdited = System.currentTimeMillis()
            )
            databaseRepository.saveUserProfile(updatedProfile)
        }
    }

    fun logout() {
        authRepository.signOut()
    }
}