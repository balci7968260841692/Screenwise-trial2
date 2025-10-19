package com.example.screentimemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_samples")
data class UsageSampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pkg: String,
    val start: Long,
    val end: Long,
    val fgSeconds: Long
)
