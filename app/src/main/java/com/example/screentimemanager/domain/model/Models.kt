package com.example.screentimemanager.domain.model

data class AppLimit(
    val id: Long,
    val type: LimitType,
    val packageOrCategory: String,
    val dailyMinutes: Int,
    val schedulesJson: String,
    val graceSeconds: Int
)

data class UsageSample(
    val id: Long,
    val pkg: String,
    val start: Long,
    val end: Long,
    val fgSeconds: Long
)

data class OverrideRequest(
    val requestId: String,
    val time: Long,
    val pkg: String,
    val requestedMins: Int,
    val userReason: String,
    val aiSummary: String,
    val contextJson: String,
    val decision: OverrideDecision,
    val grantedMins: Int?,
    val difficulty: DifficultyLevel,
    val deal: Deal?,
    val dealDueAt: Long?,
    val dealCompletedAt: Long?
)

data class TrustState(
    val userId: String = "local",
    val score: Int,
    val lastUpdate: Long,
    val recentOverrides: Int,
    val mismatches: Int,
    val honoredDeals: Int,
    val onTrackStreak: Int,
    val offTrackStreak: Int
)

data class FutureMeNote(
    val id: Long,
    val text: String,
    val createdAt: Long
)

enum class LimitType { APP, CATEGORY }

enum class OverrideDecision { PENDING, GRANTED, DENIED, NEGOTIATED }

data class Deal(
    val type: DealType,
    val description: String,
    val verify: DealVerify
)

enum class DealType { BREATHING, WALK, DEFER, CURFEW }

enum class DealVerify { TIMER, MANUAL, STEPS }

enum class DifficultyLevel { EASY, MEDIUM, HARD }
