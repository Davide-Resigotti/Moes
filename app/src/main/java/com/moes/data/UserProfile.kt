package com.moes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val userId: String,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val weightKg: Float = 0f,
    val heightCm: Float = 0f,
    val gender: String = "M",
    val birthDate: Long = 0L,
    val lastEdited: Long = 0L
)