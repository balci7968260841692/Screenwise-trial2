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
fun OverrideDialog(onComplete: () -> Unit) {
    val reason = remember { mutableStateOf("") }
    val minutes = remember { mutableStateOf("5") }
    AlertDialog(
        onDismissRequest = onComplete,
        title = { Text(stringResource(R.string.override_dialog_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = reason.value,
                    onValueChange = { reason.value = it },
                    label = { Text(stringResource(R.string.reason_label)) }
                )
                OutlinedTextField(
                    value = minutes.value,
                    onValueChange = { minutes.value = it },
                    label = { Text(stringResource(R.string.minutes_label)) },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = onComplete) { Text(stringResource(R.string.send)) }
        },
        dismissButton = {
            Button(onClick = onComplete) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun OverrideDialogHost(onComplete: () -> Unit) {
    OverrideDialog(onComplete = onComplete)
}
