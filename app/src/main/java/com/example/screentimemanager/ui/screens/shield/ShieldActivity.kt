package com.example.screentimemanager.ui.screens.shield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.screentimemanager.R
import com.example.screentimemanager.ui.theme.ScreenTimeManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@AndroidEntryPoint
class ShieldActivity : ComponentActivity() {
    private val viewModel: ShieldViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageName = intent.getStringExtra(EXTRA_PACKAGE) ?: run {
            finish()
            return
        }
        val limitMinutes = intent.getIntExtra(EXTRA_LIMIT_MINUTES, 0)
        val usedMinutes = intent.getLongExtra(EXTRA_MINUTES_USED, 0L)
        val contextJson = intent.getStringExtra(EXTRA_CONTEXT)
        viewModel.initialize(packageName, limitMinutes, usedMinutes, contextJson)
        setContent {
            ScreenTimeManagerTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.completed) {
                    if (state.completed) {
                        finish()
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(stringResource(R.string.shield_title), style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(
                            R.string.shield_usage_message,
                            state.minutesUsed,
                            state.limitMinutes
                        )
                    )
                    if (state.errorMessage != null) {
                        Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                    val showOverride = remember { mutableStateOf(false) }
                    Button(onClick = { showOverride.value = true }, enabled = state.limit != null) {
                        Text(stringResource(R.string.shield_request_button))
                    }
                    if (state.isSubmitting) {
                        CircularProgressIndicator()
                    }
                    if (showOverride.value) {
                        OverrideDialog(
                            appName = state.packageName,
                            defaultReason = stringResource(R.string.default_override_reason),
                            defaultMinutes = 5,
                            onSubmit = { reason, minutes ->
                                showOverride.value = false
                                viewModel.submitOverride(reason, minutes)
                            },
                            onDismiss = { showOverride.value = false },
                            isSubmitting = state.isSubmitting
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_PACKAGE = "extra_pkg"
        const val EXTRA_LIMIT_MINUTES = "extra_limit"
        const val EXTRA_MINUTES_USED = "extra_used"
        const val EXTRA_CONTEXT = "extra_context"
    }
}
