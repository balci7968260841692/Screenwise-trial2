package com.example.screentimemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.screentimemanager.data.local.entity.FutureMeNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FutureMeNoteDao {
    @Query("SELECT * FROM future_me_notes ORDER BY createdAt DESC")
    fun observeNotes(): Flow<List<FutureMeNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: FutureMeNoteEntity)

    @Query("DELETE FROM future_me_notes")
    suspend fun clear()
}
