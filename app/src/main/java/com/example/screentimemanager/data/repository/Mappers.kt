package com.example.screentimemanager.data.repository

import com.example.screentimemanager.data.local.entity.AppLimitEntity
import com.example.screentimemanager.data.local.entity.FutureMeNoteEntity
import com.example.screentimemanager.data.local.entity.OverrideRequestEntity
import com.example.screentimemanager.data.local.entity.TrustStateEntity
import com.example.screentimemanager.data.local.entity.UsageSampleEntity
import com.example.screentimemanager.domain.model.AppLimit
import com.example.screentimemanager.domain.model.Deal
import com.example.screentimemanager.domain.model.DealType
import com.example.screentimemanager.domain.model.DealVerify
import com.example.screentimemanager.domain.model.DifficultyLevel
import com.example.screentimemanager.domain.model.FutureMeNote
import com.example.screentimemanager.domain.model.LimitType
import com.example.screentimemanager.domain.model.OverrideDecision
import com.example.screentimemanager.domain.model.OverrideRequest
import com.example.screentimemanager.domain.model.TrustState
import com.example.screentimemanager.domain.model.UsageSample
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun AppLimitEntity.toDomain(): AppLimit = AppLimit(
    id = id,
    type = LimitType.valueOf(type),
    packageOrCategory = packageOrCategory,
    dailyMinutes = dailyMinutes,
    schedulesJson = schedulesJson,
    graceSeconds = graceSeconds
)

fun UsageSampleEntity.toDomain(): UsageSample = UsageSample(
    id = id,
    pkg = pkg,
    start = start,
    end = end,
    fgSeconds = fgSeconds
)

fun OverrideRequestEntity.toDomain(): OverrideRequest = OverrideRequest(
    requestId = requestId,
    time = time,
    pkg = pkg,
    requestedMins = requestedMins,
    userReason = userReason,
    aiSummary = aiSummaryJson,
    contextJson = contextJson,
    decision = OverrideDecision.valueOf(decision),
    grantedMins = grantedMins,
    difficulty = DifficultyLevel.valueOf(difficulty),
    deal = deal?.let { json.decodeFromString<DealSerializable>(it).toDomain() },
    dealDueAt = dealDueAt,
    dealCompletedAt = dealCompletedAt
)

fun TrustStateEntity.toDomain(): TrustState = TrustState(
    userId = userId,
    score = score,
    lastUpdate = lastUpdate,
    recentOverrides = recentOverrides,
    mismatches = mismatches,
    honoredDeals = honoredDeals,
    onTrackStreak = onTrackStreak,
    offTrackStreak = offTrackStreak
)

fun FutureMeNoteEntity.toDomain(): FutureMeNote = FutureMeNote(
    id = id,
    text = text,
    createdAt = createdAt
)

fun Deal.toJson(): String = json.encodeToString(DealSerializable(type, description, verify))

@Serializable
private data class DealSerializable(
    val type: DealType,
    val description: String,
    val verify: DealVerify
) {
    fun toDomain() = Deal(type, description, verify)
}
