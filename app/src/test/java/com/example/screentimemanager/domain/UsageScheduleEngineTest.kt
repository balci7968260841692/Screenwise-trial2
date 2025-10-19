package com.example.screentimemanager.domain

import com.example.screentimemanager.domain.logic.UsageScheduleEngine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class UsageScheduleEngineTest {
    private val engine = UsageScheduleEngine()

    @Test
    fun `quiet hours detection`() {
        val schedule = """{"mon":[{"start":"21:00","end":"23:59"}]}"""
        val now = LocalDateTime.parse("2024-01-01T22:00:00")
        assertEquals(true, engine.isWithinQuietHours(schedule, now))
    }
}
