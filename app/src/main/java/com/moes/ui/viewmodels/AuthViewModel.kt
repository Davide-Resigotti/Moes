package com.moes.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moes.repositories.AuthRepository
import com.moes.repositories.DatabaseRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var error by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    // Stato per decidere quale schermata mostrare
    var isAnonymous by mutableStateOf(authRepository.isUserAnonymous())
        private set

    init {
        // AGGIUNGI QUESTO BLOCCO INIT
        // Ascolta i cambiamenti di stato di Firebase in tempo reale
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
            isLoading = true  // Inizia caricamento
            error = null
            try {
                authRepository.linkWithEmail(email, password)
                // Il listener nel blocco init aggiornerà isAnonymous automaticamente

                val userId = authRepository.currentUserId
                databaseRepository.migrateGuestData(userId)

                databaseRepository.syncPendingSessions()
            } catch (e: Exception) {
                // Gestione errori comuni
                error = if (e.message?.contains("already in use") == true) {
                    "Email già registrata. Usa il pulsante Accedi."
                } else {
                    "Errore: ${e.message}"
                }
            } finally {
                isLoading = false // Fine caricamento
            }
        }
    }

    fun onLoginClick() {
        if (email.isBlank() || password.isBlank()) {
            error = "Inserisci email e password"
            return
        }

        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                Log.d("AUTH_DEBUG", "1. Inizio Login...")
                authRepository.signInWithEmail(email, password)
                Log.d("AUTH_DEBUG", "2. Login Firebase completato!")

                val userId = authRepository.currentUserId
                Log.d("AUTH_DEBUG", "3. ID Utente recuperato: $userId")

                // SE SI BLOCCA QUI: Il problema è il Database Room
                databaseRepository.migrateGuestData(userId)
                Log.d("AUTH_DEBUG", "4. Migrazione dati locali completata")

                // SE SI BLOCCA QUI: Il problema è Firestore/Internet
                databaseRepository.syncPendingSessions()
                Log.d("AUTH_DEBUG", "5. Sync completato")

                // 3. NUOVO: Scarica i dati storici dal Cloud al telefono
                Log.d("AUTH_DEBUG", "Inizio download dati cloud...")
                databaseRepository.syncFromCloud(userId)
                Log.d("AUTH_DEBUG", "Download completato")

            } catch (e: Exception) {
                Log.e("AUTH_DEBUG", "ERRORE: ${e.message}")
                error = "Login fallito: ${e.message}"
            } finally {
                isLoading = false
                Log.d("AUTH_DEBUG", "6. Fine operazione (isLoading = false)")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}