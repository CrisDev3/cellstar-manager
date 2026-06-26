package com.example.cs_manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores the current shopping cart as a JSON blob so it survives app restarts.
 * Only ever has one row (id = 1).
 */
@Entity(tableName = "cart_pref")
data class CartPrefEntity(
    @PrimaryKey val id: Int = 1,
    val itemsJson: String // JSON serialized List<SerializableCartItem>
)
