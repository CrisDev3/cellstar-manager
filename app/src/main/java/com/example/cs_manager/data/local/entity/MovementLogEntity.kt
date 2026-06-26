package com.example.cs_manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movement_logs")
data class MovementLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateTime: String,
    val user: String,
    val action: String,
    val actionColor: String // "green", "blue", "orange", "red"
)
