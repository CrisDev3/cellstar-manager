package com.example.cs_manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores the last logged-in username so it persists across app restarts.
 * Only ever has one row (id = 1).
 */
@Entity(tableName = "logged_user_pref")
data class LoggedUserPrefEntity(
    @PrimaryKey val id: Int = 1,
    val username: String
)
