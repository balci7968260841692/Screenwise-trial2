package com.example.screentimemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.screentimemanager.data.local.entity.TrustStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrustStateDao {
    @Query("SELECT * FROM trust_state WHERE userId = :userId")
    fun observe(userId: String = "local"): Flow<TrustStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TrustStateEntity)
}
