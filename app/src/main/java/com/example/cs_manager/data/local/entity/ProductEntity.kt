package com.example.cs_manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val sku: String,
    val name: String,
    val model: String,
    val color: String = "",
    val description: String = "",
    val category: String,
    val price: Double,
    val stock: Int,
    val minStock: Int,
    val imagePath: String? = null
)
