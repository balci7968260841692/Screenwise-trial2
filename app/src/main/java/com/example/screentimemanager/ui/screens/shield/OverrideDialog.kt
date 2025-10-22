package com.example.screentimemanager.ui.screens.shield

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.screentimemanager.R

@Composable
fun OverrideDialog(
    appName: String,
    defaultReason: String,
    defaultMinutes: Int,
    onSubmit: (reason: String, minutes: Int) -> Unit,
    onDismiss: () -> Unit,
    isSubmitting: Boolean
) {
    val reason = remember { mutableStateOf(defaultReason) }
    val minutes = remember { mutableStateOf(defaultMinutes.toString()) }
    val error = remember { mutableStateOf<String?>(null) }
    val minutesError = stringResource(R.string.override_dialog_error_minutes)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.override_dialog_title_format, appName)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = reason.value,
                    onValueChange = { reason.value = it },
                    label = { Text(stringResource(R.string.reason_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = minutes.value,
                    onValueChange = {
                        minutes.value = it
                        error.value = null
                    },
                    label = { Text(stringResource(R.string.minutes_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                if (error.value != null) {
                    Text(
                        text = error.value!!,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val minutesValue = minutes.value.toIntOrNull()
                    if (minutesValue == null || minutesValue <= 0) {
                        error.value = minutesError
                        return@Button
                    }
                    onSubmit(reason.value, minutesValue)
                },
                enabled = !isSubmitting
            ) {
                Text(stringResource(R.string.override_dialog_submit))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, enabled = !isSubmitting) {
                Text(stringResource(R.string.override_dialog_cancel))
            }
        }
    )
}
