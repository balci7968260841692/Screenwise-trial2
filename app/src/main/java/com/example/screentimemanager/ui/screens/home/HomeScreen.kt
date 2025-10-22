package com.example.screentimemanager.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.screentimemanager.R
import com.example.screentimemanager.domain.model.AppLimit
import com.example.screentimemanager.ui.screens.shield.OverrideDialog

@Composable
fun HomeScreen(
    padding: PaddingValues,
    state: HomeUiState,
    onRequestOverride: (AppLimit, String, Int) -> Unit,
    onOpenCoach: () -> Unit,
    onOpenSettings: () -> Unit,
    onManageLimits: () -> Unit
) {
    val selectedLimit = remember { mutableStateOf<LimitUsageUi?>(null) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.trust_score_label, state.trustScore),
                style = MaterialTheme.typography.titleLarge
            )
        }
        item {
            Text(
                text = stringResource(R.string.difficulty_label, state.difficulty.name),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenCoach, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(id = R.string.label_weekly_coach))
                }
                Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(id = R.string.label_settings))
                }
                Button(onClick = onManageLimits, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(id = R.string.home_manage_limits))
                }
            }
        }
        if (state.limitStatuses.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.home_no_limits_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            item {
                Text(
                    stringResource(R.string.home_no_limits_body),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            item {
                Text(
                    text = stringResource(id = R.string.home_limits_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(state.limitStatuses) { limitStatus ->
                LimitCard(limitStatus = limitStatus, onRequestOverride = {
                    selectedLimit.value = limitStatus
                })
            }
        }
        if (state.recentOverrides.isNotEmpty()) {
            item {
                val latest = state.recentOverrides.first()
                Text(
                    text = stringResource(
                        R.string.home_last_decision,
                        latest.pkg,
                        latest.decision.name
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    val limitForDialog = selectedLimit.value
    if (limitForDialog != null) {
        OverrideDialog(
            appName = limitForDialog.limit.packageOrCategory,
            defaultReason = stringResource(R.string.default_override_reason),
            defaultMinutes = limitForDialog.minutesRemaining.toInt().coerceAtLeast(5),
            onSubmit = { reason, minutes ->
                onRequestOverride(limitForDialog.limit, reason, minutes)
                selectedLimit.value = null
            },
            onDismiss = { selectedLimit.value = null },
            isSubmitting = false
        )
    }
}

@Composable
private fun LimitCard(limitStatus: LimitUsageUi, onRequestOverride: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = limitStatus.limit.packageOrCategory, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(
                    R.string.home_time_used,
                    limitStatus.minutesUsed,
                    limitStatus.limit.dailyMinutes
                )
            )
            Text(
                text = stringResource(
                    R.string.home_time_remaining,
                    limitStatus.minutesRemaining
                )
            )
            if (limitStatus.inQuietHours) {
                Text(
                    text = stringResource(R.string.home_quiet_hours_badge),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Button(onClick = onRequestOverride) {
                Text(stringResource(id = R.string.action_request_override))
            }
        }
    }
}
