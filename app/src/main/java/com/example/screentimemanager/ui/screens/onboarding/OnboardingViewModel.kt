package com.example.screentimemanager.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screentimemanager.data.repository.LimitsRepository
import com.example.screentimemanager.domain.model.AppLimit
import com.example.screentimemanager.domain.model.LimitType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val limitsRepository: LimitsRepository
) : ViewModel() {

    val limits: StateFlow<List<AppLimit>> = limitsRepository.observeLimits()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun addLimit(packageName: String, minutes: Int, grace: Int) {
        if (_isSaving.value) return
        _isSaving.value = true
        viewModelScope.launch {
            try {
                limitsRepository.create(
                    type = LimitType.APP,
                    packageOrCategory = packageName,
                    minutes = minutes,
                    schedulesJson = "",
                    grace = grace
                )
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deleteLimit(id: Long) {
        viewModelScope.launch {
            limitsRepository.delete(id)
        }
    }
}
