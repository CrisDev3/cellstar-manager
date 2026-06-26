package com.example.cs_manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cs_manager.data.local.entity.LoggedUserPrefEntity

@Dao
interface LoggedUserPrefDao {

    @Query("SELECT * FROM logged_user_pref WHERE id = 1")
    suspend fun get(): LoggedUserPrefEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pref: LoggedUserPrefEntity)

    @Query("DELETE FROM logged_user_pref WHERE id = 1")
    suspend fun delete()
}
