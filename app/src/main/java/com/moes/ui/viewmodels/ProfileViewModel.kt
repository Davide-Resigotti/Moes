package com.moes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moes.data.UserProfile
import com.moes.data.UserStatistics
import com.moes.repositories.AuthRepository
import com.moes.repositories.DatabaseRepository
import com.moes.utils.BerghemNameGenerator
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
            val isGuest = userId == AuthRepository.GUEST_ID
            val currentEmail = authRepository.currentUserEmail.lowercase()

            val isNameless = profile == null || profile.firstName.isBlank()

            if (isGuest && isNameless) {
                val (genName, genSurname) = BerghemNameGenerator.generate()

                val newProfile = profile?.copy(
                    firstName = genName,
                    lastName = genSurname,
                    lastEdited = System.currentTimeMillis()
                ) ?: UserProfile(
                    userId = userId,
                    firstName = genName,
                    lastName = genSurname,
                    lastEdited = System.currentTimeMillis()
                )

                saveProfileInternal(newProfile)
                newProfile

            } else if (profile == null) {
                val emptyProfile = UserProfile(
                    userId = userId,
                    email = currentEmail
                )

                viewModelScope.launch {
                    databaseRepository.saveLocalUserProfile(emptyProfile)
                }
                emptyProfile

            } else {
                if (!isGuest && currentEmail.isNotBlank() && profile.email != currentEmail) {
                    val updatedProfile = profile.copy(
                        email = currentEmail,
                        lastEdited = System.currentTimeMillis()
                    )
                    saveProfileInternal(updatedProfile)
                    updatedProfile
                } else {
                    profile
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile(userId = authRepository.currentUserIdSafe)
    )

    val userStatistics: StateFlow<UserStatistics?> = currentUserId.flatMapLatest { userId ->
        databaseRepository.getUserStatisticsFlow(userId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun saveProfile(
        firstName: String,
        lastName: String,
        weight: String,
        height: String,
        gender: String,
        birthDate: Long
    ) {
        if (firstName.isBlank()) return;

        viewModelScope.launch {
            val w = weight.trim().toFloatOrNull() ?: 0f
            val h = height.trim().toFloatOrNull() ?: 0f

            val activeUserId = currentUserId.value
            val currentProfile = userProfile.value

            val updatedProfile = currentProfile.copy(
                userId = activeUserId,
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                weightKg = w,
                heightCm = h,
                gender = gender,
                birthDate = birthDate,
                lastEdited = System.currentTimeMillis()
            )
            databaseRepository.saveUserProfile(updatedProfile)
        }
    }

    private fun saveProfileInternal(profile: UserProfile) {
        viewModelScope.launch {
            databaseRepository.saveUserProfile(profile)
        }
    }

    fun logout() {
        authRepository.signOut()
    }
}