package com.example.cs_manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registered_users")
data class RegisteredUserEntity(
    @PrimaryKey val username: String,
    val fullName: String,
    val email: String,
    val password: String
)
