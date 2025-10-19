package com.example.screentimemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.screentimemanager.data.local.entity.OverrideRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OverrideRequestDao {
    @Query("SELECT * FROM override_requests ORDER BY time DESC")
    fun observeRequests(): Flow<List<OverrideRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(request: OverrideRequestEntity)

    @Query("SELECT SUM(grantedMins) FROM override_requests WHERE time >= :start")
    suspend fun sumGrantedMinutesSince(start: Long): Int?
}
