package com.example.cs_manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cs_manager.data.local.entity.MovementLogEntity

@Dao
interface MovementLogDao {

    @Query("SELECT * FROM movement_logs ORDER BY id DESC")
    suspend fun getAll(): List<MovementLogEntity>

    @Insert
    suspend fun insert(log: MovementLogEntity)

    @Query("DELETE FROM movement_logs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM movement_logs")
    suspend fun count(): Int
}
