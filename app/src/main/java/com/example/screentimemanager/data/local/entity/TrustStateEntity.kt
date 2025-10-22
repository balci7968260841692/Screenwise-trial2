package com.example.screentimemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trust_state")
data class TrustStateEntity(
    @PrimaryKey val userId: String = "local",
    val score: Int,
    val lastUpdate: Long,
    val recentOverrides: Int,
    val mismatches: Int,
    val honoredDeals: Int,
    val onTrackStreak: Int,
    val offTrackStreak: Int
)
