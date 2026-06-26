package com.example.cs_manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cs_manager.data.local.entity.CartPrefEntity

@Dao
interface CartPrefDao {

    @Query("SELECT * FROM cart_pref WHERE id = 1")
    suspend fun get(): CartPrefEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cart: CartPrefEntity)

    @Query("DELETE FROM cart_pref WHERE id = 1")
    suspend fun delete()
}
