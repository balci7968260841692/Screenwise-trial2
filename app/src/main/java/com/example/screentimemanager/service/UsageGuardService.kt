package com.example.screentimemanager.service

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.screentimemanager.data.repository.LimitsRepository
import com.example.screentimemanager.domain.logic.UsageScheduleEngine
import com.example.screentimemanager.domain.model.AppLimit
import com.example.screentimemanager.domain.model.LimitType
import com.example.screentimemanager.ui.screens.shield.ShieldActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import org.json.JSONObject

@AndroidEntryPoint
class UsageGuardService : AccessibilityService() {

    @Inject lateinit var limitsRepository: LimitsRepository
    @Inject lateinit var scheduleEngine: UsageScheduleEngine

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val appLimits = MutableStateFlow<Map<String, AppLimit>>(emptyMap())
    private val lastPromptTimestamps = mutableMapOf<String, Long>()
    private val usageStatsManager by lazy { getSystemService(UsageStatsManager::class.java) }

    override fun onServiceConnected() {
        super.onServiceConnected()
        scope.launch {
            limitsRepository.observeLimits().collect { limits ->
                appLimits.value = limits.filter { it.type == LimitType.APP }
                    .associateBy { it.packageOrCategory }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        val limit = appLimits.value[packageName] ?: return
        scope.launch {
            val now = System.currentTimeMillis()
            val usageMs = queryForegroundTime(packageName, now)
            val inQuietHours = scheduleEngine.isWithinQuietHours(limit.schedulesJson)
            val limitMs = limit.dailyMinutes * 60_000L
            val graceMs = limit.graceSeconds * 1000L
            val beyondLimit = usageMs >= limitMs + graceMs
            if ((beyondLimit || inQuietHours) && shouldPrompt(packageName, now)) {
                launchShield(packageName, limit, usageMs, inQuietHours)
            }
        }
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun shouldPrompt(packageName: String, now: Long): Boolean {
        val lastPrompt = lastPromptTimestamps[packageName] ?: 0L
        if (now - lastPrompt < PROMPT_COOLDOWN_MS) {
            return false
        }
        lastPromptTimestamps[packageName] = now
        return true
    }

    private fun launchShield(
        packageName: String,
        limit: AppLimit,
        usageMs: Long,
        quietHours: Boolean
    ) {
        performGlobalAction(GLOBAL_ACTION_HOME)
        val intent = Intent(this, ShieldActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(EXTRA_PACKAGE, packageName)
            putExtra(EXTRA_LIMIT_MINUTES, limit.dailyMinutes)
            putExtra(EXTRA_MINUTES_USED, usageMs / 60_000L)
            putExtra(EXTRA_CONTEXT, buildContextSnapshot(packageName, usageMs, quietHours))
        }
        startActivity(intent)
    }

    private fun queryForegroundTime(packageName: String, now: Long): Long {
        val manager = usageStatsManager ?: return 0L
        val startOfDay = LocalDate.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return try {
            manager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startOfDay,
                now
            ).firstOrNull { it.packageName == packageName }?.totalTimeInForeground ?: 0L
        } catch (security: SecurityException) {
            0L
        }
    }

    private fun buildContextSnapshot(
        packageName: String,
        usageMs: Long,
        quietHours: Boolean
    ): String {
        val now = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        return JSONObject().apply {
            put("app", packageName)
            put("localTime", now.toLocalTime().format(formatter))
            put("dow", now.dayOfWeek.name.take(3))
            put("network", "unknown")
            put("recentOverrides30m", 0)
            put("weeklyTrend", "0%")
            put("minutesUsed", usageMs / 60_000L)
            put("quietHours", quietHours)
        }.toString()
    }

    companion object {
        private const val PROMPT_COOLDOWN_MS = 10_000L
        const val EXTRA_PACKAGE = "extra_pkg"
        const val EXTRA_LIMIT_MINUTES = "extra_limit"
        const val EXTRA_MINUTES_USED = "extra_used"
        const val EXTRA_CONTEXT = "extra_context"
    }
}
