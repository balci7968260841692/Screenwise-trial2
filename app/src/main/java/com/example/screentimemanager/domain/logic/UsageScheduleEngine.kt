package com.example.screentimemanager.domain.logic

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class UsageScheduleEngine(
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    fun isWithinQuietHours(scheduleJson: String, now: LocalDateTime = LocalDateTime.now()): Boolean {
        if (scheduleJson.isBlank()) return false
        val root = json.parseToJsonElement(scheduleJson)
        if (root !is JsonObject) return false
        val dayKey = now.dayOfWeek.name.lowercase()
        val quiet = root[dayKey] ?: return false
        if (quiet !is JsonArray) return false
        val current = now.toLocalTime()
        return quiet.any { entry ->
            if (entry !is JsonObject) return@any false
            val start = entry["start"]?.jsonPrimitive?.contentOrNull ?: return@any false
            val end = entry["end"]?.jsonPrimitive?.contentOrNull ?: return@any false
            val startTime = LocalTime.parse(start, formatter)
            val endTime = LocalTime.parse(end, formatter)
            if (startTime <= endTime) {
                current in startTime..endTime
            } else {
                current >= startTime || current <= endTime
            }
        }
    }

    fun dailyMinutesUsed(samples: List<Pair<Long, Long>>): Long =
        samples.sumOf { (start, end) -> (end - start).coerceAtLeast(0L) } / 60_000
}

private val JsonElement.jsonPrimitive: JsonPrimitive
    get() = this as JsonPrimitive

private val JsonPrimitive.contentOrNull: String?
    get() = if (isString) content else null
