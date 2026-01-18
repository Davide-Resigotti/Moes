package com.moes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val userId: String,
    val firstName: String = "",
    val lastName: String = "",
    val weightKg: Float = 0f,
    val heightCm: Float = 0f,
    val gender: String = "M",
    val birthYear: Int = 0,
    val profilePictureUrl: String? = null,
    val lastEdited: Long = 0L
) {
    val fullName: String
        get() = if (firstName.isBlank() && lastName.isBlank()) "Utente" else "$firstName $lastName".trim()
}