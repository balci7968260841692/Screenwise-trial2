package com.example.screentimemanager.ui.screens.shield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.screentimemanager.R
import com.example.screentimemanager.ui.theme.ScreenTimeManagerTheme

class ShieldActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenTimeManagerTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(stringResource(R.string.shield_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.shield_body))
                    val showOverride = remember { mutableStateOf(false) }
                    Button(onClick = { showOverride.value = true }) {
                        Text(stringResource(R.string.shield_request_button))
                    }
                    if (showOverride.value) {
                        OverrideDialog(onComplete = { finish() })
                    }
                }
            }
        }
    }
}
