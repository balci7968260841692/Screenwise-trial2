package com.example.screentimemanager.data.repository

import com.example.screentimemanager.data.local.dao.AppLimitDao
import com.example.screentimemanager.data.local.entity.AppLimitEntity
import com.example.screentimemanager.domain.model.AppLimit
import com.example.screentimemanager.domain.model.LimitType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LimitsRepository(private val dao: AppLimitDao) {
    fun observeLimits(): Flow<List<AppLimit>> = dao.observeLimits().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun upsert(limit: AppLimit) {
        dao.upsert(
            AppLimitEntity(
                id = limit.id,
                type = limit.type.name,
                packageOrCategory = limit.packageOrCategory,
                dailyMinutes = limit.dailyMinutes,
                schedulesJson = limit.schedulesJson,
                graceSeconds = limit.graceSeconds
            )
        )
    }

    suspend fun create(
        type: LimitType,
        packageOrCategory: String,
        minutes: Int,
        schedulesJson: String,
        grace: Int
    ) {
        dao.upsert(
            AppLimitEntity(
                type = type.name,
                packageOrCategory = packageOrCategory,
                dailyMinutes = minutes,
                schedulesJson = schedulesJson,
                graceSeconds = grace
            )
        )
    }
}
