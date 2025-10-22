package com.example.screentimemanager.domain

import com.example.screentimemanager.domain.logic.TrustCalculator
import com.example.screentimemanager.domain.model.DifficultyLevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TrustCalculatorTest {
    private val calculator = TrustCalculator()

    @Test
    fun `decay moves score toward baseline`() {
        val state = TrustCalculator.defaultState(now = 0).copy(score = 80)
        val decayed = calculator.applyEvent(state, TrustCalculator.Event.WeeklyDecay(now = 1))
        assertEquals(true, decayed.score < state.score)
    }

    @Test
    fun `denied override reduces score`() {
        val state = TrustCalculator.defaultState(now = 0)
        val result = calculator.applyEvent(
            state,
            TrustCalculator.Event.OverrideProcessed(
                granted = false,
                mismatches = 1,
                honoredDeal = false,
                difficulty = DifficultyLevel.MEDIUM,
                now = 1
            )
        )
        assertEquals(true, result.score < state.score)
    }
}
