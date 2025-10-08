package com.example.screentimemanager.data.repository

import com.example.screentimemanager.data.local.dao.FutureMeNoteDao
import com.example.screentimemanager.data.local.entity.FutureMeNoteEntity
import com.example.screentimemanager.data.remote.CoachPayload
import com.example.screentimemanager.data.remote.CoachTipsResponse
import com.example.screentimemanager.data.remote.SupabaseFunctionClient
import com.example.screentimemanager.domain.model.FutureMeNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CoachRepository(
    private val noteDao: FutureMeNoteDao,
    private val functionClient: SupabaseFunctionClient,
    private val json: Json = Json
) {
    fun observeNotes(): Flow<List<FutureMeNote>> =
        noteDao.observeNotes().map { it.map { entity -> entity.toDomain() } }

    suspend fun addNote(text: String) {
        noteDao.upsert(FutureMeNoteEntity(text = text, createdAt = System.currentTimeMillis()))
    }

    suspend fun fetchCoachTips(weeklyUsage: Map<String, Any?>): CoachTipsResponse {
        val payload = CoachPayload(weeklyUsage = json.encodeToString(weeklyUsage))
        return functionClient.requestCoachTips(payload)
    }
}

private fun FutureMeNoteEntity.toDomain() = FutureMeNote(
    id = id,
    text = text,
    createdAt = createdAt
)
