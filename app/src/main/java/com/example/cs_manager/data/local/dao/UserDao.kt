package com.example.cs_manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cs_manager.data.local.entity.RegisteredUserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM registered_users")
    suspend fun getAll(): List<RegisteredUserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<RegisteredUserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: RegisteredUserEntity)

    @Query("DELETE FROM registered_users")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM registered_users")
    suspend fun count(): Int
}
