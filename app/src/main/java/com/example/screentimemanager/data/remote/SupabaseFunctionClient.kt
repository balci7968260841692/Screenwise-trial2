package com.example.screentimemanager.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SupabaseFunctionClient(private val client: SupabaseClient) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun requestNegotiation(request: NegotiationPayload): NegotiationResponse {
        val response = client.functions.invoke("ai_negotiation") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(request))
        }
        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun requestCoachTips(payload: CoachPayload): CoachTipsResponse {
        val response = client.functions.invoke("ai_negotiation") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(payload.copy(mode = "coach")))
        }
        return json.decodeFromString(response.bodyAsText())
    }
}

@Serializable
data class NegotiationPayload(
    val reason: String,
    val context: String
)

@Serializable
data class NegotiationResponse(
    val summary: String,
    val intent: String,
    val urgency: String,
    val sincerity: Double,
    val mismatches: List<String> = emptyList(),
    val alignment: Double,
    val counterMinutes: Int?,
    val deal: DealContract? = null,
    val tone: String
)

@Serializable
data class DealContract(
    val type: String,
    val description: String,
    val verify: String
)

@Serializable
data class CoachPayload(
    val weeklyUsage: String,
    val mode: String = "coach"
)

@Serializable
data class CoachTipsResponse(
    val tips: List<String>
)
