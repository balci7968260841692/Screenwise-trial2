package com.example.screentimemanager.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.screentimemanager.R

@Composable
fun OnboardingScreen(onFinished: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val limits by viewModel.limits.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val packageInput = rememberSaveable { mutableStateOf("") }
    val minutesInput = rememberSaveable { mutableStateOf("60") }
    val graceInput = rememberSaveable { mutableStateOf("60") }
    val error = remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.onboarding_intro_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.onboarding_intro_body),
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = packageInput.value,
            onValueChange = {
                packageInput.value = it
                error.value = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.onboarding_package_label)) }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = minutesInput.value,
                onValueChange = {
                    minutesInput.value = it
                    error.value = null
                },
                label = { Text(stringResource(R.string.onboarding_minutes_label)) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = graceInput.value,
                onValueChange = {
                    graceInput.value = it
                    error.value = null
                },
                label = { Text(stringResource(R.string.onboarding_grace_label)) },
                modifier = Modifier.weight(1f)
            )
        }
        if (error.value != null) {
            Text(error.value!!, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = {
                val minutes = minutesInput.value.toIntOrNull()
                val grace = graceInput.value.toIntOrNull()
                val pkg = packageInput.value.trim()
                if (pkg.isEmpty() || minutes == null || grace == null) {
                    error.value = stringResource(R.string.onboarding_error_invalid)
                } else {
                    viewModel.addLimit(pkg, minutes, grace)
                    packageInput.value = ""
                    minutesInput.value = "60"
                    graceInput.value = "60"
                }
            },
            enabled = !isSaving
        ) {
            Text(stringResource(R.string.onboarding_add_limit))
        }
        LazyColumn(
            modifier = Modifier.weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(limits) { limit ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(limit.packageOrCategory, style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.onboarding_limit_minutes, limit.dailyMinutes))
                        Text(stringResource(R.string.onboarding_limit_grace, limit.graceSeconds))
                        Button(onClick = { viewModel.deleteLimit(limit.id) }, enabled = !isSaving) {
                            Text(stringResource(R.string.onboarding_remove_limit))
                        }
                    }
                }
            }
        }
        Button(onClick = onFinished, enabled = limits.isNotEmpty()) {
            Text(stringResource(R.string.label_onboarding_finish))
        }
    }
}
