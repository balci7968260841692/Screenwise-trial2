package com.example.screentimemanager.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.screentimemanager.ui.screens.coach.WeeklyCoachScreen
import com.example.screentimemanager.ui.screens.home.HomeScreen
import com.example.screentimemanager.ui.screens.home.HomeViewModel
import com.example.screentimemanager.ui.screens.onboarding.OnboardingScreen
import com.example.screentimemanager.ui.screens.settings.SettingsScreen

enum class AppDestination(val route: String) {
    Onboarding("onboarding"),
    Home("home"),
    Coach("coach"),
    Settings("settings")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    padding: PaddingValues
) {
    NavHost(navController = navController, startDestination = AppDestination.Onboarding.route) {
        composable(AppDestination.Onboarding.route) {
            OnboardingScreen(onFinished = {
                navController.navigate(AppDestination.Home.route) {
                    popUpTo(AppDestination.Onboarding.route) { inclusive = true }
                    launchSingleTop = true
                }
            })
        }
        composable(AppDestination.Home.route) {
            val vm: HomeViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            HomeScreen(
                padding = padding,
                state = state,
                onRequestOverride = { limit, reason, minutes ->
                    vm.onRequestOverride(limit, reason, minutes)
                },
                onOpenCoach = { navController.navigate(AppDestination.Coach.route) },
                onOpenSettings = { navController.navigate(AppDestination.Settings.route) },
                onManageLimits = { navController.navigate(AppDestination.Onboarding.route) }
            )
        }
        composable(AppDestination.Coach.route) {
            WeeklyCoachScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
