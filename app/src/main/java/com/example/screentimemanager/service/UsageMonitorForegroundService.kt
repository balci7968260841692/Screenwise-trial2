package com.example.screentimemanager.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.screentimemanager.R
import com.example.screentimemanager.data.repository.LimitsRepository
import com.example.screentimemanager.data.repository.UsageRepository
import com.example.screentimemanager.domain.model.AppLimit
import com.example.screentimemanager.domain.model.LimitType
import com.example.screentimemanager.domain.model.UsageSample
import com.example.screentimemanager.worker.WeeklyCoachWorker
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UsageMonitorForegroundService : Service() {

    @Inject lateinit var limitsRepository: LimitsRepository
    @Inject lateinit var usageRepository: UsageRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val trackedLimits = MutableStateFlow<List<AppLimit>>(emptyList())
    private val usageStatsManager by lazy { getSystemService(UsageStatsManager::class.java) }
    private val lastTotals = mutableMapOf<String, Long>()

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
        scheduleCoachWorker()
        observeLimits()
        startUsageCollection()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        ContextCompat.startForegroundService(
            applicationContext,
            Intent(applicationContext, UsageMonitorForegroundService::class.java)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun buildNotification(): Notification {
        val channelId = "usage_monitor_channel"
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Screen time monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            manager?.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_monitor_active))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    private fun observeLimits() {
        scope.launch {
            limitsRepository.observeLimits().collect { limits ->
                val appLimits = limits.filter { it.type == LimitType.APP }
                trackedLimits.value = appLimits
                val currentPackages = appLimits.map { it.packageOrCategory }.toSet()
                lastTotals.keys.retainAll(currentPackages)
            }
        }
    }

    private fun startUsageCollection() {
        scope.launch {
            while (true) {
                collectUsageSnapshot()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun collectUsageSnapshot() {
        val manager = usageStatsManager ?: return
        val limits = trackedLimits.value
        if (limits.isEmpty()) return
        val now = System.currentTimeMillis()
        val startOfDay = LocalDate.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val stats = try {
            manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfDay, now)
        } catch (security: SecurityException) {
            emptyList<UsageStats>()
        }
        if (stats.isEmpty()) return
        val samples = mutableListOf<UsageSample>()
        limits.forEach { limit ->
            val usage = stats.firstOrNull { it.packageName == limit.packageOrCategory } ?: return@forEach
            val total = usage.totalTimeInForeground
            val previous = lastTotals[usage.packageName] ?: 0L
            val delta = (total - previous).coerceAtLeast(0L)
            if (delta >= MIN_SAMPLE_DURATION_MS) {
                val end = now
                val start = end - delta
                samples += UsageSample(
                    id = 0,
                    pkg = usage.packageName,
                    start = start,
                    end = end,
                    fgSeconds = TimeUnit.MILLISECONDS.toSeconds(delta)
                )
            }
            lastTotals[usage.packageName] = total
        }
        if (samples.isNotEmpty()) {
            usageRepository.insert(samples)
        }
    }

    private fun scheduleCoachWorker() {
        val request = PeriodicWorkRequestBuilder<WeeklyCoachWorker>(7, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weekly_coach",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    companion object {
        private const val NOTIFICATION_ID = 101
        private const val POLL_INTERVAL_MS = 30_000L
        private const val MIN_SAMPLE_DURATION_MS = 15_000L
    }
}
