package com.example.screentimemanager.domain.logic

import com.example.screentimemanager.domain.model.DifficultyLevel
import com.example.screentimemanager.domain.model.TrustState
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class TrustCalculator {

    sealed class Event {
        data class OverrideProcessed(
            val granted: Boolean,
            val mismatches: Int,
            val honoredDeal: Boolean,
            val difficulty: DifficultyLevel,
            val now: Long
        ) : Event()

        data class WeeklyDecay(val now: Long) : Event()
    }

    fun applyEvent(state: TrustState, event: Event): TrustState = when (event) {
        is Event.OverrideProcessed -> {
            val basePenalty = if (event.granted) 2 else 5
            val mismatchPenalty = event.mismatches * 4
            val difficultyBonus = when (event.difficulty) {
                DifficultyLevel.EASY -> 1
                DifficultyLevel.MEDIUM -> 3
                DifficultyLevel.HARD -> 5
            }
            val honoredBonus = if (event.honoredDeal) difficultyBonus else 0
            val scoreDelta = honoredBonus - (basePenalty + mismatchPenalty)
            val updatedScore = clampScore(state.score + scoreDelta)
            state.copy(
                score = updatedScore,
                lastUpdate = event.now,
                recentOverrides = state.recentOverrides + 1,
                mismatches = state.mismatches + event.mismatches,
                honoredDeals = state.honoredDeals + if (event.honoredDeal) 1 else 0,
                onTrackStreak = if (event.granted && mismatchPenalty == 0) state.onTrackStreak + 1 else 0,
                offTrackStreak = if (!event.granted || mismatchPenalty > 0) state.offTrackStreak + 1 else 0
            )
        }
        is Event.WeeklyDecay -> {
            val towardBaseline = (60 - state.score) * 0.15
            val newScore = clampScore((state.score + towardBaseline).roundToInt())
            state.copy(score = newScore, lastUpdate = event.now)
        }
    }

    fun difficultyForScore(score: Int): DifficultyLevel = when {
        score >= 80 -> DifficultyLevel.EASY
        score >= 50 -> DifficultyLevel.MEDIUM
        else -> DifficultyLevel.HARD
    }

    companion object {
        fun defaultState(now: Long) = TrustState(
            userId = "local",
            score = 60,
            lastUpdate = now,
            recentOverrides = 0,
            mismatches = 0,
            honoredDeals = 0,
            onTrackStreak = 0,
            offTrackStreak = 0
        )
    }
}

private fun clampScore(value: Int) = min(100, max(0, value))
