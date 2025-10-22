package com.example.screentimemanager.data.repository

import com.example.screentimemanager.data.local.dao.UsageSampleDao
import com.example.screentimemanager.data.local.entity.UsageSampleEntity
import com.example.screentimemanager.domain.model.UsageSample
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UsageRepository(private val dao: UsageSampleDao) {
    fun observeSince(since: Long): Flow<List<UsageSample>> =
        dao.observeSince(since).map { it.map { entity -> entity.toDomain() } }

    suspend fun insert(samples: List<UsageSample>) {
        dao.insertAll(samples.map {
            UsageSampleEntity(
                id = it.id,
                pkg = it.pkg,
                start = it.start,
                end = it.end,
                fgSeconds = it.fgSeconds
            )
        })
    }

    suspend fun totalForegroundSeconds(pkg: String, since: Long): Long =
        dao.totalForegroundSeconds(pkg, since)
}
