package com.example.screentimemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.screentimemanager.data.local.entity.UsageSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageSampleDao {
    @Query("SELECT * FROM usage_samples WHERE start >= :since ORDER BY start DESC")
    fun observeSince(since: Long): Flow<List<UsageSampleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(samples: List<UsageSampleEntity>)

    @Query("DELETE FROM usage_samples")
    suspend fun clear()
}
