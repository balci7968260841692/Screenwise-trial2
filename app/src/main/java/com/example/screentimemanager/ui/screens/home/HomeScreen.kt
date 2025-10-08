package com.example.screentimemanager.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.screentimemanager.R
import com.example.screentimemanager.domain.model.OverrideRequest

@Composable
fun HomeScreen(
    padding: PaddingValues,
    state: HomeUiState,
    onRequestOverride: (OverrideRequestDraft) -> Unit,
    onOpenCoach: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = stringResource(R.string.trust_score_label, state.trustScore), style = MaterialTheme.typography.titleLarge)
        Text(text = stringResource(R.string.difficulty_label, state.difficulty.name), style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onOpenCoach) { Text(stringResource(id = R.string.label_weekly_coach)) }
        Button(onClick = onOpenSettings) { Text(stringResource(id = R.string.label_settings)) }
        Button(onClick = {
            onRequestOverride(
                OverrideRequestDraft(
                    packageName = "demo.app",
                    requestedMinutes = 5,
                    reason = stringResource(id = R.string.default_override_reason),
                    contextJson = "{\"app\":\"demo.app\",\"localTime\":\"12:00\"}"
                )
            )
        }) { Text(stringResource(id = R.string.action_request_override)) }
        Text(text = stringResource(id = R.string.recent_overrides_title), style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.recentOverrides) { override ->
                OverrideCard(override)
            }
        }
    }
}

@Composable
fun OverrideCard(request: OverrideRequest) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = request.pkg, style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(R.string.override_card_body, request.requestedMins, request.decision.name))
        }
    }
}
