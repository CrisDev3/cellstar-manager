package com.example.cs_manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cs_manager.data.local.entity.SaleTransactionEntity

@Dao
interface SaleTransactionDao {

    @Query("SELECT * FROM sale_transactions ORDER BY dateTime DESC")
    suspend fun getAll(): List<SaleTransactionEntity>

    @Insert
    suspend fun insert(transaction: SaleTransactionEntity)

    @Query("DELETE FROM sale_transactions")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM sale_transactions")
    suspend fun count(): Int
}
