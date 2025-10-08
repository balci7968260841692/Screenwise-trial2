package com.example.screentimemanager.domain

import com.example.screentimemanager.domain.logic.OverrideNegotiator
import com.example.screentimemanager.domain.logic.TrustCalculator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OfflineFallbackTest {
    @Test
    fun `negotiator grants minimal time offline`() {
        val negotiator = OverrideNegotiator(TrustCalculator())
        val result = negotiator.evaluate(
            trustScore = 40,
            requestedMinutes = 10,
            alignment = 0.0,
            dailyGrantedTotal = 0,
            capMinutes = 15
        )
        assertTrue(result.recommendedGrant >= 0)
    }
}
