package com.devx.flashtrack.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.*
import com.devx.flashtrack.ui.screens.accounts.AccountsScreen
import com.devx.flashtrack.ui.screens.analysis.AnalysisScreen
import com.devx.flashtrack.ui.screens.categories.CategoriesScreen
import com.devx.flashtrack.ui.screens.debts.DebtsScreen
import com.devx.flashtrack.ui.screens.home.HomeScreen
import com.devx.flashtrack.ui.screens.onboarding.OnboardingScreen
import com.devx.flashtrack.ui.screens.reminders.RemindersScreen
import com.devx.flashtrack.ui.screens.settings.SettingsScreen
import com.devx.flashtrack.ui.screens.transaction.AddTransactionScreen
import com.devx.flashtrack.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    object Onboarding     : Screen("onboarding")
    object Home           : Screen("home")
    object AddTransaction : Screen("add_transaction?type={type}") {
        fun createRoute(type: String = "EXPENSE") = "add_transaction?type=$type"
    }
    object Accounts       : Screen("accounts")
    object Analysis       : Screen("analysis")
    object Reminders      : Screen("reminders")
    object Debts          : Screen("debts")
    object Categories     : Screen("categories")
    object Settings       : Screen("settings")
}

@Composable
fun FlashTrackNavGraph(
    navController: NavHostController,
    viewModel: MainViewModel,
    startDestination: String
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination,
        enterTransition  = { slideInHorizontally(tween(280)) { it / 6 } + fadeIn(tween(280)) },
        exitTransition   = { slideOutHorizontally(tween(280)) { -it / 6 } + fadeOut(tween(280)) },
        popEnterTransition  = { slideInHorizontally(tween(280)) { -it / 6 } + fadeIn(tween(280)) },
        popExitTransition   = { slideOutHorizontally(tween(280)) { it / 6 } + fadeOut(tween(280)) }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinish = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onAddTransaction = { type -> navController.navigate(Screen.AddTransaction.createRoute(type)) },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(
            route     = Screen.AddTransaction.route,
            arguments = listOf(navArgument("type") { defaultValue = "EXPENSE" })
        ) { back ->
            AddTransactionScreen(
                viewModel   = viewModel,
                initialType = back.arguments?.getString("type") ?: "EXPENSE",
                onBack      = { navController.popBackStack() }
            )
        }

        composable(Screen.Accounts.route) {
            AccountsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Screen.Analysis.route) {
            AnalysisScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Screen.Reminders.route) {
            RemindersScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Screen.Debts.route) {
            DebtsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
