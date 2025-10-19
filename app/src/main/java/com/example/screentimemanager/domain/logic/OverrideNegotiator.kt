package com.example.screentimemanager.domain.logic

import com.example.screentimemanager.domain.model.DifficultyLevel
import kotlin.math.roundToInt

class OverrideNegotiator(private val trustCalculator: TrustCalculator) {

    data class Result(
        val recommendedGrant: Int,
        val counterMinutes: Int?,
        val requireDeal: Boolean,
        val difficulty: DifficultyLevel
    )

    fun evaluate(
        trustScore: Int,
        requestedMinutes: Int,
        alignment: Double,
        dailyGrantedTotal: Int,
        capMinutes: Int
    ): Result {
        val difficulty = trustCalculator.difficultyForScore(trustScore)
        val baseGrant = when (difficulty) {
            DifficultyLevel.EASY -> minOf(requestedMinutes, 15)
            DifficultyLevel.MEDIUM -> minOf(requestedMinutes, 10)
            DifficultyLevel.HARD -> minOf(requestedMinutes, 5)
        }
        val alignmentBoost = (alignment * 5).roundToInt()
        val capRemaining = (capMinutes - dailyGrantedTotal).coerceAtLeast(0)
        val recommended = (baseGrant + alignmentBoost).coerceAtMost(capRemaining)
        val requireDeal = requestedMinutes > 20 && difficulty != DifficultyLevel.EASY
        val counter = if (requireDeal) minOf(10, requestedMinutes / 3) else null
        return Result(
            recommendedGrant = recommended,
            counterMinutes = counter,
            requireDeal = requireDeal,
            difficulty = difficulty
        )
    }
}
