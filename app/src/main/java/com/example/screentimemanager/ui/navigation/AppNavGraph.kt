package com.example.screentimemanager.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.screentimemanager.ui.screens.coach.WeeklyCoachScreen
import com.example.screentimemanager.ui.screens.home.HomeScreen
import com.example.screentimemanager.ui.screens.home.HomeViewModel
import com.example.screentimemanager.ui.screens.onboarding.OnboardingScreen
import com.example.screentimemanager.ui.screens.settings.SettingsScreen
import com.example.screentimemanager.ui.screens.shield.OverrideDialogHost

enum class AppDestination(val route: String) {
    Onboarding("onboarding"),
    Home("home"),
    Shield("shield"),
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
                }
            })
        }
        composable(AppDestination.Home.route) {
            val vm: HomeViewModel = hiltViewModel()
            HomeScreen(
                padding = padding,
                state = vm.uiState,
                onRequestOverride = { vm.onRequestOverride(it) },
                onOpenCoach = { navController.navigate(AppDestination.Coach.route) },
                onOpenSettings = { navController.navigate(AppDestination.Settings.route) }
            )
        }
        composable(AppDestination.Coach.route) {
            WeeklyCoachScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Shield.route) {
            OverrideDialogHost(onComplete = { navController.popBackStack() })
        }
    }
}
