package com.example.cs_manager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cs_manager.data.local.dao.CartPrefDao
import com.example.cs_manager.data.local.dao.LoggedUserPrefDao
import com.example.cs_manager.data.local.dao.MovementLogDao
import com.example.cs_manager.data.local.dao.ProductDao
import com.example.cs_manager.data.local.dao.SaleTransactionDao
import com.example.cs_manager.data.local.dao.UserDao
import com.example.cs_manager.data.local.entity.CartPrefEntity
import com.example.cs_manager.data.local.entity.LoggedUserPrefEntity
import com.example.cs_manager.data.local.entity.MovementLogEntity
import com.example.cs_manager.data.local.entity.ProductEntity
import com.example.cs_manager.data.local.entity.RegisteredUserEntity
import com.example.cs_manager.data.local.entity.SaleTransactionEntity

@Database(
    entities = [
        ProductEntity::class,
        RegisteredUserEntity::class,
        MovementLogEntity::class,
        SaleTransactionEntity::class,
        LoggedUserPrefEntity::class,
        CartPrefEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun userDao(): UserDao
    abstract fun movementLogDao(): MovementLogDao
    abstract fun saleTransactionDao(): SaleTransactionDao
    abstract fun loggedUserPrefDao(): LoggedUserPrefDao
    abstract fun cartPrefDao(): CartPrefDao
}
