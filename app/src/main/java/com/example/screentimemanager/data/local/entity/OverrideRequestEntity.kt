package com.example.screentimemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "override_requests")
data class OverrideRequestEntity(
    @PrimaryKey val requestId: String,
    val time: Long,
    val pkg: String,
    val requestedMins: Int,
    val userReason: String,
    val aiSummaryJson: String,
    val contextJson: String,
    val decision: String,
    val grantedMins: Int?,
    val difficulty: String,
    val deal: String?,
    val dealDueAt: Long?,
    val dealCompletedAt: Long?
)
