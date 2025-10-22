package com.example.screentimemanager.ui.screens.shield

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.screentimemanager.BuildConfig
import com.example.screentimemanager.data.repository.LimitsRepository
import com.example.screentimemanager.data.repository.OverridesRepository
import com.example.screentimemanager.data.repository.TrustRepository
import com.example.screentimemanager.data.repository.UsageRepository
import com.example.screentimemanager.domain.model.AppLimit
import com.example.screentimemanager.domain.model.OverrideDecision
import com.example.screentimemanager.domain.model.TrustState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

@HiltViewModel
class ShieldViewModel @Inject constructor(
    application: Application,
    private val limitsRepository: LimitsRepository,
    private val overridesRepository: OverridesRepository,
    private val usageRepository: UsageRepository,
    trustRepository: TrustRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ShieldUiState())
    val uiState: StateFlow<ShieldUiState> = _uiState.asStateFlow()

    private var trustState: TrustState? = null
    private var contextSnapshot: String? = null

    init {
        viewModelScope.launch {
            trustRepository.observe().collectLatest { trust ->
                trustState = trust
                _uiState.update { it.copy(trustScore = trust.score) }
            }
        }
    }

    fun initialize(packageName: String, limitMinutes: Int?, usedMinutes: Long?, contextJson: String?) {
        if (_uiState.value.packageName.isNotEmpty()) return
        contextSnapshot = contextJson
        _uiState.update {
            it.copy(
                packageName = packageName,
                limitMinutes = limitMinutes ?: 0,
                minutesUsed = usedMinutes ?: 0L
            )
        }
        viewModelScope.launch {
            val limit = limitsRepository.findAppLimit(packageName)
            if (limit == null) {
                _uiState.update { current ->
                    current.copy(errorMessage = "No limit configured for $packageName")
                }
                return@launch
            }
            val usage = usageRepository.totalForegroundSeconds(
                packageName,
                startOfDayMillis()
            )
            val minutesUsed = TimeUnit.SECONDS.toMinutes(usage)
            _uiState.update {
                it.copy(
                    limit = limit,
                    limitMinutes = limit.dailyMinutes,
                    minutesUsed = minutesUsed
                )
            }
        }
    }

    fun submitOverride(reason: String, minutes: Int) {
        val limit = _uiState.value.limit ?: return
        val trust = trustState ?: return
        if (_uiState.value.isSubmitting) return
        val contextJson = contextSnapshot ?: buildContext(limit)
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val outcome = overridesRepository.submitOverride(
                    pkg = limit.packageOrCategory,
                    requestedMinutes = minutes,
                    reason = reason,
                    contextJson = contextJson,
                    dailyGrantedTotal = _uiState.value.grantedToday,
                    dailyCap = BuildConfig.DEFAULT_DAILY_OVERRIDE_CAP,
                    trustState = trust
                )
                _uiState.update {
                    it.copy(
                        lastDecision = outcome.decision,
                        grantedToday = it.grantedToday + outcome.grantedMinutes,
                        isSubmitting = false,
                        completed = true
                    )
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = t.message ?: "Unknown error")
                }
            }
        }
    }

    private fun buildContext(limit: AppLimit): String {
        val now = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        return JSONObject().apply {
            put("app", limit.packageOrCategory)
            put("localTime", now.toLocalTime().format(formatter))
            put("dow", now.dayOfWeek.name.take(3))
            put("network", "unknown")
            put("recentOverrides30m", 0)
            put("weeklyTrend", "0%")
            put("limitMinutes", limit.dailyMinutes)
        }.toString()
    }

    private fun startOfDayMillis(): Long {
        val zone = ZoneId.systemDefault()
        return LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
    }
}

data class ShieldUiState(
    val packageName: String = "",
    val limit: AppLimit? = null,
    val limitMinutes: Int = 0,
    val minutesUsed: Long = 0,
    val trustScore: Int = 60,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val lastDecision: OverrideDecision? = null,
    val grantedToday: Int = 0,
    val completed: Boolean = false
)
