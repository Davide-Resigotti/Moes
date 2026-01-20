package com.moes.repositories

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    companion object {
        const val GUEST_ID = "moes_guest_user"
    }

    val currentUserIdSafe: String
        get() = auth.currentUser?.uid ?: GUEST_ID

    val currentUserId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User ID not found!")

    val currentUserEmail: String
        get() = auth.currentUser?.email ?: ""

    fun signOut() {
        auth.signOut()
    }

    suspend fun linkWithEmail(email: String, pass: String) {
        if (auth.currentUser == null) {
            auth.createUserWithEmailAndPassword(email, pass).await()
        } else {
            val credential = EmailAuthProvider.getCredential(email, pass)
            auth.currentUser?.linkWithCredential(credential)?.await()
        }
    }

    suspend fun signInWithEmail(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass).await()
    }

    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    fun isUserAnonymous(): Boolean {
        val user = auth.currentUser
        return user == null || user.isAnonymous
    }

    fun addAuthStateListener(listener: (Boolean) -> Unit) {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            val isAnon = user == null || user.isAnonymous
            listener(isAnon)
        }
    }
}