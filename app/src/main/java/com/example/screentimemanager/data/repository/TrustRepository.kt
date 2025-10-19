package com.example.screentimemanager.data.repository

import com.example.screentimemanager.data.local.dao.TrustStateDao
import com.example.screentimemanager.data.local.entity.TrustStateEntity
import com.example.screentimemanager.domain.logic.TrustCalculator
import com.example.screentimemanager.domain.model.TrustState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Clock

class TrustRepository(
    private val dao: TrustStateDao,
    private val trustCalculator: TrustCalculator,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    fun observe(): Flow<TrustState> = dao.observe().map { entity ->
        entity?.toDomain() ?: TrustCalculator.defaultState(clock.millis()).also {
            dao.upsert(it.toEntity())
        }
    }

    suspend fun update(block: (TrustState) -> TrustState) {
        val current = dao.observe().first()?.toDomain() ?: TrustCalculator.defaultState(clock.millis())
        dao.upsert(block(current).toEntity())
    }

    private fun TrustState.toEntity() = TrustStateEntity(
        userId = userId,
        score = score,
        lastUpdate = lastUpdate,
        recentOverrides = recentOverrides,
        mismatches = mismatches,
        honoredDeals = honoredDeals,
        onTrackStreak = onTrackStreak,
        offTrackStreak = offTrackStreak
    )
}
