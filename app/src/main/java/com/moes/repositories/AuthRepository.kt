package com.moes.repositories

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    companion object {
        // ID fisso per l'utente non loggato
        const val GUEST_ID = "moes_guest_user"
    }

    // Proprietà che non crasha mai:
    // Ritorna l'UID reale se esiste, altrimenti l'ID ospite fisso.
    val currentUserIdSafe: String
        get() = auth.currentUser?.uid ?: GUEST_ID

    // Proprietà classica (può servire altrove, ma qui usiamo quella safe)
    val currentUserId: String
        get() = auth.currentUser?.uid
            ?: throw IllegalStateException("User ID not found!")

    /**
     * Da chiamare all'avvio dell'app.
     * Se l'utente non c'è, ne crea uno anonimo invisibile.
     */
    suspend fun initializeAuth() {
        if (auth.currentUser == null) {
            try {
                auth.signInAnonymously().await()
            } catch (e: Exception) {
                // Gestisci l'errore (es. riprova o mostra errore fatale)
                e.printStackTrace()
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    /**
     * REGISTRAZIONE: Collega l'attuale utente anonimo a una mail/password.
     * I dati locali e remoti vengono mantenuti perché l'UID non cambia.
     */
    suspend fun linkWithEmail(email: String, pass: String) {
        if (auth.currentUser == null) {
            // FALLBACK: Se l'utente è null (init fallita), creiamo un account da zero
            auth.createUserWithEmailAndPassword(email, pass).await()
        } else {
            // STANDARD: Se l'utente esiste (anonimo), lo colleghiamo
            val credential = EmailAuthProvider.getCredential(email, pass)
            auth.currentUser?.linkWithCredential(credential)?.await()
        }
    }

    /**
     * LOGIN: Accede a un account esistente.
     * ATTENZIONE: Questo cambia l'UID dell'utente corrente!
     */
    suspend fun signInWithEmail(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass).await()
    }

    fun isUserAnonymous(): Boolean {
        val user = auth.currentUser
        // Se user è null, non è loggato -> trattiamolo come anonimo/da loggare
        // Se user.isAnonymous è true -> è anonimo
        return user == null || user.isAnonymous
    }

    // AGGIUNGI QUESTA FUNZIONE PER IL LISTENER
    fun addAuthStateListener(listener: (Boolean) -> Unit) {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            // Notifica true se l'utente è null o anonimo (mostra LoginScreen)
            // Notifica false se è un utente reale (mostra AccountScreen)
            val isAnon = user == null || user.isAnonymous
            listener(isAnon)
        }
    }
}