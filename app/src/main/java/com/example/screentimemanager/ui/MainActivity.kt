package com.example.screentimemanager.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.example.screentimemanager.ui.navigation.AppNavGraph
import com.example.screentimemanager.ui.theme.ScreenTimeManagerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenTimeManagerTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val navController = rememberNavController()
                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                    AppNavGraph(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        padding = padding
                    )
                }
            }
        }
    }
}
