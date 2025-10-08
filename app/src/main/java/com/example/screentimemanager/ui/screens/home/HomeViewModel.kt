package com.example.screentimemanager.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screentimemanager.BuildConfig
import com.example.screentimemanager.data.repository.LimitsRepository
import com.example.screentimemanager.data.repository.OverridesRepository
import com.example.screentimemanager.data.repository.TrustRepository
import com.example.screentimemanager.domain.model.OverrideDecision
import com.example.screentimemanager.domain.model.TrustState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    limitsRepository: LimitsRepository,
    private val overridesRepository: OverridesRepository,
    trustRepository: TrustRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState get() = _uiState.value

    private var latestTrust: TrustState? = null

    init {
        combine(
            limitsRepository.observeLimits(),
            overridesRepository.observe(),
            trustRepository.observe()
        ) { limits, overrides, trust ->
            latestTrust = trust
            HomeUiState(
                limits = limits,
                trustScore = trust.score,
                difficulty = trustRepositoryDifficulty(trust.score),
                recentOverrides = overrides.take(5),
                lastDecision = _uiState.value.lastDecision,
                grantedToday = _uiState.value.grantedToday
            )
        }.onEach { newState ->
            _uiState.value = newState
        }.launchIn(viewModelScope)
    }

    fun onRequestOverride(request: OverrideRequestDraft) {
        val trust = latestTrust ?: return
        viewModelScope.launch {
            val outcome = overridesRepository.submitOverride(
                pkg = request.packageName,
                requestedMinutes = request.requestedMinutes,
                reason = request.reason,
                contextJson = request.contextJson,
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
}

data class HomeUiState(
    val limits: List<com.example.screentimemanager.domain.model.AppLimit> = emptyList(),
    val trustScore: Int = 60,
    val difficulty: DifficultyBadge = DifficultyBadge.Medium,
    val recentOverrides: List<com.example.screentimemanager.domain.model.OverrideRequest> = emptyList(),
    val lastDecision: OverrideDecision? = null,
    val grantedToday: Int = 0
)

data class OverrideRequestDraft(
    val packageName: String,
    val requestedMinutes: Int,
    val reason: String,
    val contextJson: String
)

enum class DifficultyBadge { Easy, Medium, Hard }
