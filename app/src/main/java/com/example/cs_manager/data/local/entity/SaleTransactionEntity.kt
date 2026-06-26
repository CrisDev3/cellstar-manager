package com.example.cs_manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for SaleTransaction.
 * 'itemsJson' stores the list of CartItem as a JSON string (serialized with Gson).
 */
@Entity(tableName = "sale_transactions")
data class SaleTransactionEntity(
    @PrimaryKey val id: String,
    val dateTime: String,
    val clientName: String,
    val clientId: String,
    val itemsJson: String,  // JSON serialized List<CartItem>
    val totalAmount: Double
)
