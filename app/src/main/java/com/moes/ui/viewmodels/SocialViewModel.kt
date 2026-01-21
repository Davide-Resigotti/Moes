package com.moes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moes.data.UserStatistics
import com.moes.data.social.Friend
import com.moes.data.social.FriendRequest
import com.moes.repositories.AuthRepository
import com.moes.repositories.SocialRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SocialUiState(
    val friends: List<Friend> = emptyList(),
    val pendingRequests: List<FriendRequest> = emptyList(),
    val sentRequests: List<FriendRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class SocialViewModel(
    private val socialRepository: SocialRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState(isLoading = true))
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    private val currentUserId = MutableStateFlow(authRepository.currentUserIdSafe)

    init {
        authRepository.addAuthStateListener {
            currentUserId.value = authRepository.currentUserIdSafe
        }
        startObserving()
    }

    private fun startObserving() {
        viewModelScope.launch {
            launch {
                currentUserId.flatMapLatest { userId ->
                    socialRepository.getMyFriends(userId)
                }.catch { e ->
                    _uiState.update { it.copy(friends = emptyList(), isLoading = false) }
                }.collect { friends ->
                    _uiState.update { it.copy(friends = friends, isLoading = false) }
                }
            }

            launch {
                currentUserId.flatMapLatest { userId ->
                    socialRepository.getIncomingRequests(userId)
                }.catch { e ->
                    _uiState.update { it.copy(pendingRequests = emptyList()) }
                }.collect { requests ->
                    _uiState.update { it.copy(pendingRequests = requests) }
                }
            }

            launch {
                currentUserId.flatMapLatest { userId ->
                    socialRepository.getSentRequests(userId)
                }.catch { e ->
                    _uiState.update { it.copy(sentRequests = emptyList()) }
                }.collect { sent ->
                    _uiState.update { it.copy(sentRequests = sent) }
                }
            }
        }
    }

    fun getFriendInfo(friendId: String): Friend? {
        return _uiState.value.friends.find { it.userId == friendId }
    }

    fun getFriendStats(friendId: String): Flow<UserStatistics?> {
        return socialRepository.getFriendStatistics(friendId)
    }

    fun sendFriendRequest(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Inserisci un'email valida") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

            val result = socialRepository.sendRequest(email.trim())

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(isLoading = false, successMessage = "Richiesta inviata a $email")
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Errore sconosciuto"
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    fun acceptRequest(request: FriendRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = socialRepository.acceptRequest(request)
            handleRequestResult(result, "Richiesta accettata! Siete amici.")
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = socialRepository.rejectRequest(requestId)
            handleRequestResult(result, "Richiesta rifiutata.")
        }
    }

    fun cancelSentRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = socialRepository.cancelSentRequest(requestId)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(isLoading = false, successMessage = "Richiesta annullata.")
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Errore: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = socialRepository.removeFriend(friendId)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(isLoading = false, successMessage = "Amico rimosso.")
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Errore rimozione: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    private fun handleRequestResult(result: Result<Unit>, successMsg: String) {
        if (result.isSuccess) {
            _uiState.update { it.copy(isLoading = false, successMessage = successMsg) }
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Operazione fallita: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}