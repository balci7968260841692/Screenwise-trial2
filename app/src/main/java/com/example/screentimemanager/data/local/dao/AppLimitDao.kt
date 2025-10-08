package com.example.screentimemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.screentimemanager.data.local.entity.AppLimitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLimitDao {
    @Query("SELECT * FROM app_limits")
    fun observeLimits(): Flow<List<AppLimitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(limit: AppLimitEntity)

    @Query("DELETE FROM app_limits WHERE id = :id")
    suspend fun delete(id: Long)
}
