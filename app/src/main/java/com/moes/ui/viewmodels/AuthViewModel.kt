package com.moes.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moes.repositories.AuthRepository
import com.moes.repositories.DatabaseRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var error by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    var isAnonymous by mutableStateOf(authRepository.isUserAnonymous())
        private set

    private val _loginCompletedEvent = Channel<Unit>()
    val loginCompletedEvent = _loginCompletedEvent.receiveAsFlow()

    init {
        authRepository.addAuthStateListener { isAnon ->
            isAnonymous = isAnon
        }
    }

    fun onRegisterClick() {
        if (email.isBlank() || password.isBlank()) {
            error = "Inserisci email e password"
            return
        }
        viewModelScope.launch {
            performAuthAction {
                authRepository.linkWithEmail(email, password)
            }
        }
    }

    fun onLoginClick() {
        if (email.isBlank() || password.isBlank()) {
            error = "Inserisci email e password"
            return
        }
        viewModelScope.launch {
            performAuthAction {
                authRepository.signInWithEmail(email, password)
            }
        }
    }

    fun onGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            performAuthAction {
                authRepository.signInWithGoogle(idToken)
            }
        }
    }

    fun onGoogleSignInError(e: Exception) {
        error = "Accesso con Google fallito. Riprova."
    }

    private suspend fun performAuthAction(action: suspend () -> Unit) {
        isLoading = true
        error = null
        try {
            action()
            handleSuccessfulLogin()

            _loginCompletedEvent.send(Unit)
        } catch (e: Exception) {
            handleAuthError(e)
        } finally {
            isLoading = false
        }
    }

    private suspend fun handleSuccessfulLogin() {
        val userId = authRepository.currentUserId
        databaseRepository.migrateGuestData(userId)
        databaseRepository.syncPendingSessions()
        databaseRepository.syncFromCloud(userId)
    }

    private fun handleAuthError(e: Exception) {
        Log.e("AUTH_ERROR", "Errore: ${e.message}")
        error = if (e.message?.contains("already in use") == true) {
            "Email già registrata. Usa il pulsante Accedi."
        } else {
            "Errore: ${e.message}"
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}