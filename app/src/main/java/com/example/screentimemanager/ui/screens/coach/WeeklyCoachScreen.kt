package com.example.screentimemanager.ui.screens.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.screentimemanager.R

@Composable
fun WeeklyCoachScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.coach_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.coach_body), style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onBack) { Text(stringResource(R.string.back)) }
    }
}
