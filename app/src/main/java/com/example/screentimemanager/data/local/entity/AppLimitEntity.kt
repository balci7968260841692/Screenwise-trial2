package com.example.screentimemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_limits")
data class AppLimitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val packageOrCategory: String,
    val dailyMinutes: Int,
    val schedulesJson: String,
    val graceSeconds: Int
)
