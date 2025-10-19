package com.example.screentimemanager.domain

import com.example.screentimemanager.domain.logic.OverrideNegotiator
import com.example.screentimemanager.domain.logic.TrustCalculator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OverrideNegotiatorTest {
    private val negotiator = OverrideNegotiator(TrustCalculator())

    @Test
    fun `requires deal on large request with medium trust`() {
        val result = negotiator.evaluate(
            trustScore = 60,
            requestedMinutes = 30,
            alignment = 0.5,
            dailyGrantedTotal = 0,
            capMinutes = 60
        )
        assertEquals(true, result.requireDeal)
    }
}
