package com.example.screentimemanager.data.repository

import com.example.screentimemanager.data.local.dao.OverrideRequestDao
import com.example.screentimemanager.data.local.dao.TrustStateDao
import com.example.screentimemanager.data.local.entity.OverrideRequestEntity
import com.example.screentimemanager.data.local.entity.TrustStateEntity
import com.example.screentimemanager.data.remote.DealContract
import com.example.screentimemanager.data.remote.NegotiationPayload
import com.example.screentimemanager.data.remote.SupabaseFunctionClient
import com.example.screentimemanager.domain.logic.OverrideNegotiator
import com.example.screentimemanager.domain.logic.TrustCalculator
import com.example.screentimemanager.domain.model.Deal
import com.example.screentimemanager.domain.model.DealType
import com.example.screentimemanager.domain.model.DealVerify
import com.example.screentimemanager.domain.model.DifficultyLevel
import com.example.screentimemanager.domain.model.OverrideDecision
import com.example.screentimemanager.domain.model.TrustState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class OverridesRepository(
    private val overrideDao: OverrideRequestDao,
    private val trustDao: TrustStateDao,
    private val trustCalculator: TrustCalculator,
    private val functionClient: SupabaseFunctionClient,
    private val json: Json = Json
) {

    data class OverrideOutcome(
        val decision: OverrideDecision,
        val grantedMinutes: Int,
        val difficulty: DifficultyLevel,
        val deal: Deal?,
        val tone: String,
        val summary: String,
        val alignment: Double
    )

    fun observe(): Flow<List<com.example.screentimemanager.domain.model.OverrideRequest>> =
        overrideDao.observeRequests().map { entities -> entities.map { it.toDomain() } }

    suspend fun submitOverride(
        pkg: String,
        requestedMinutes: Int,
        reason: String,
        contextJson: String,
        dailyGrantedTotal: Int,
        dailyCap: Int,
        trustState: TrustState
    ): OverrideOutcome {
        val requestId = UUID.randomUUID().toString()
        val negotiationResponse = functionClient.requestNegotiation(
            NegotiationPayload(reason = reason, context = contextJson)
        )
        val negotiator = OverrideNegotiator(trustCalculator)
        val evaluation = negotiator.evaluate(
            trustScore = trustState.score,
            requestedMinutes = requestedMinutes,
            alignment = negotiationResponse.alignment,
            dailyGrantedTotal = dailyGrantedTotal,
            capMinutes = dailyCap
        )
        val grantedMinutes = evaluation.recommendedGrant
        val decision = when {
            grantedMinutes <= 0 -> OverrideDecision.DENIED
            grantedMinutes >= requestedMinutes -> OverrideDecision.GRANTED
            evaluation.counterMinutes != null -> OverrideDecision.NEGOTIATED
            else -> OverrideDecision.GRANTED
        }
        val deal = evaluation.requireDeal
            .takeIf { it && negotiationResponse.deal != null }
            ?.let { mapDeal(negotiationResponse.deal!!) }
        val now = System.currentTimeMillis()
        overrideDao.upsert(
            OverrideRequestEntity(
                requestId = requestId,
                time = now,
                pkg = pkg,
                requestedMins = requestedMinutes,
                userReason = reason,
                aiSummaryJson = negotiationResponse.summary,
                contextJson = contextJson,
                decision = decision.name,
                grantedMins = grantedMinutes,
                difficulty = evaluation.difficulty.name,
                deal = deal?.let { json.encodeToString(DealSerializable(it)) },
                dealDueAt = deal?.let { now + DEAL_DEFAULT_WINDOW_MS },
                dealCompletedAt = null
            )
        )
        val trustEvent = TrustCalculator.Event.OverrideProcessed(
            granted = decision != OverrideDecision.DENIED,
            mismatches = negotiationResponse.mismatches.size,
            honoredDeal = false,
            difficulty = evaluation.difficulty,
            now = now
        )
        trustDao.upsert(
            trustCalculator.applyEvent(trustState, trustEvent).toEntity()
        )
        return OverrideOutcome(
            decision = decision,
            grantedMinutes = grantedMinutes,
            difficulty = evaluation.difficulty,
            deal = deal,
            tone = negotiationResponse.tone,
            summary = negotiationResponse.summary,
            alignment = negotiationResponse.alignment
        )
    }

    private fun mapDeal(contract: DealContract): Deal = Deal(
        type = when (contract.type.lowercase()) {
            "breathing" -> DealType.BREATHING
            "walk" -> DealType.WALK
            "defer" -> DealType.DEFER
            else -> DealType.CURFEW
        },
        description = contract.description,
        verify = when (contract.verify.lowercase()) {
            "timer" -> DealVerify.TIMER
            "steps" -> DealVerify.STEPS
            else -> DealVerify.MANUAL
        }
    )

    private fun com.example.screentimemanager.domain.model.TrustState.toEntity() = TrustStateEntity(
        userId = userId,
        score = score,
        lastUpdate = lastUpdate,
        recentOverrides = recentOverrides,
        mismatches = mismatches,
        honoredDeals = honoredDeals,
        onTrackStreak = onTrackStreak,
        offTrackStreak = offTrackStreak
    )

    @kotlinx.serialization.Serializable
    private data class DealSerializable(val type: DealType, val description: String, val verify: DealVerify)

    companion object {
        private const val DEAL_DEFAULT_WINDOW_MS = 20 * 60 * 1000L
    }
}
