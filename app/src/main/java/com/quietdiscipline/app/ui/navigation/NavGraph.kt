package com.quietdiscipline.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.quietdiscipline.app.ui.freeze.FreezeScreen
import com.quietdiscipline.app.ui.home.HomeScreen
import com.quietdiscipline.app.ui.settings.SettingsScreen
import com.quietdiscipline.app.ui.guide.GuideScreen
import com.quietdiscipline.app.ui.stats.StatsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToStats = {
                    navController.navigate(Routes.STATS)
                },
                onNavigateToGuide = {
                    navController.navigate(Routes.GUIDE)
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.STATS) {
            StatsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.GUIDE) {
            GuideScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.FREEZE,
            arguments = listOf(
                navArgument("packageName") { type = NavType.StringType },
                navArgument("freezeMinutes") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
            val freezeMinutes = backStackEntry.arguments?.getInt("freezeMinutes") ?: 5

            FreezeScreen(
                frozenPackage = packageName,
                freezeMinutes = freezeMinutes
            )
        }
    }
}
