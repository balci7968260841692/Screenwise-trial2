package com.example.screentimemanager.ui.screens.home

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.screentimemanager.BuildConfig
import com.example.screentimemanager.data.repository.LimitsRepository
import com.example.screentimemanager.data.repository.OverridesRepository
import com.example.screentimemanager.data.repository.TrustRepository
import com.example.screentimemanager.data.repository.UsageRepository
import com.example.screentimemanager.domain.logic.UsageScheduleEngine
import com.example.screentimemanager.domain.model.AppLimit
import com.example.screentimemanager.domain.model.OverrideDecision
import com.example.screentimemanager.domain.model.OverrideRequest
import com.example.screentimemanager.domain.model.TrustState
import com.example.screentimemanager.domain.model.UsageSample
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONObject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val limitsRepository: LimitsRepository,
    private val overridesRepository: OverridesRepository,
    private val trustRepository: TrustRepository,
    private val usageRepository: UsageRepository,
    private val scheduleEngine: UsageScheduleEngine
) : AndroidViewModel(application) {

    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var latestTrust: TrustState? = null

    init {
        combine(
            limitsRepository.observeLimits(),
            overridesRepository.observe(),
            trustRepository.observe(),
            usageRepository.observeSince(startOfDayMillis())
        ) { limits, overrides, trust, usageSamples ->
            latestTrust = trust
            val usageByPackage = usageSamples.groupBy { it.pkg }.mapValues { entry ->
                TimeUnit.SECONDS.toMinutes(entry.value.sumOf(UsageSample::fgSeconds))
            }
            val limitStatuses = limits.map { limit ->
                val minutesUsed = usageByPackage[limit.packageOrCategory] ?: 0L
                val minutesRemaining = (limit.dailyMinutes.toLong() - minutesUsed).coerceAtLeast(0)
                LimitUsageUi(
                    limit = limit,
                    minutesUsed = minutesUsed,
                    minutesRemaining = minutesRemaining,
                    inQuietHours = scheduleEngine.isWithinQuietHours(limit.schedulesJson)
                )
            }
            val grantedToday = overrides
                .filter { it.time >= startOfDayMillis() }
                .mapNotNull { it.grantedMins }
                .sum()
            HomeUiState(
                limitStatuses = limitStatuses,
                trustScore = trust.score,
                difficulty = trustRepositoryDifficulty(trust.score),
                recentOverrides = overrides.take(5),
                lastDecision = _uiState.value.lastDecision,
                grantedToday = grantedToday
            )
        }.onEach { newState ->
            _uiState.value = newState
        }.launchIn(viewModelScope)
    }

    fun onRequestOverride(limit: AppLimit, reason: String, requestedMinutes: Int) {
        val trust = latestTrust ?: return
        val status = _uiState.value.limitStatuses.firstOrNull { it.limit.id == limit.id }
        viewModelScope.launch {
            val outcome = overridesRepository.submitOverride(
                pkg = limit.packageOrCategory,
                requestedMinutes = requestedMinutes,
                reason = reason,
                contextJson = buildContextSnapshot(limit, status),
                dailyGrantedTotal = _uiState.value.grantedToday,
                dailyCap = BuildConfig.DEFAULT_DAILY_OVERRIDE_CAP,
                trustState = trust
            )
            _uiState.value = _uiState.value.copy(
                lastDecision = outcome.decision,
                grantedToday = _uiState.value.grantedToday + outcome.grantedMinutes
            )
        }
    }

    private fun trustRepositoryDifficulty(score: Int) = when {
        score >= 80 -> DifficultyBadge.Easy
        score >= 50 -> DifficultyBadge.Medium
        else -> DifficultyBadge.Hard
    }

    private fun buildContextSnapshot(limit: AppLimit, status: LimitUsageUi?): String {
        val now = ZonedDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        val network = connectivityManager?.let { manager ->
            val active = manager.activeNetwork ?: return@let "unknown"
            val caps = manager.getNetworkCapabilities(active) ?: return@let "unknown"
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
                else -> "unknown"
            }
        } ?: "unknown"
        val recentOverrides = _uiState.value.recentOverrides.count {
            System.currentTimeMillis() - it.time <= THIRTY_MINUTES_MS
        }
        return JSONObject().apply {
            put("app", limit.packageOrCategory)
            put("localTime", now.toLocalTime().format(formatter))
            put("dow", now.dayOfWeek.name.take(3))
            put("network", network)
            put("recentOverrides30m", recentOverrides)
            put("weeklyTrend", "0%")
            put("difficulty", _uiState.value.difficulty.name.lowercase(Locale.getDefault()))
            status?.let {
                put("minutesRemaining", it.minutesRemaining)
                put("minutesUsed", it.minutesUsed)
                put("quietHours", it.inQuietHours)
            }
        }.toString()
    }

    private fun startOfDayMillis(): Long {
        val zone = ZoneId.systemDefault()
        return LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
    }
}

data class HomeUiState(
    val limitStatuses: List<LimitUsageUi> = emptyList(),
    val trustScore: Int = 60,
    val difficulty: DifficultyBadge = DifficultyBadge.Medium,
    val recentOverrides: List<OverrideRequest> = emptyList(),
    val lastDecision: OverrideDecision? = null,
    val grantedToday: Int = 0
)

data class LimitUsageUi(
    val limit: AppLimit,
    val minutesUsed: Long,
    val minutesRemaining: Long,
    val inQuietHours: Boolean
)

enum class DifficultyBadge { Easy, Medium, Hard }

private const val THIRTY_MINUTES_MS = 30 * 60 * 1000L
