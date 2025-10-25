package com.lessup.medledger.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lessup.medledger.ui.home.HomeScreen
import com.lessup.medledger.ui.chronic.ChronicScreen
import com.lessup.medledger.ui.settings.SettingsScreen

sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Dest("home", "首页", Icons.Outlined.Home)
    data object Chronic : Dest("chronic", "慢病", Icons.Outlined.FavoriteBorder)
    data object Settings : Dest("settings", "设置", Icons.Outlined.Settings)
}

val bottomDestinations = listOf(Dest.Home, Dest.Chronic, Dest.Settings)

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Dest.Home.route) {
        composable(Dest.Home.route) { HomeScreen(onEdit = { id -> if (id == null) navController.navigate("visit/edit") else navController.navigate("visit/edit/$id") }) }
        composable(Dest.Chronic.route) { ChronicScreen() }
        composable(Dest.Settings.route) { SettingsScreen() }
        composable("visit/edit") { com.lessup.medledger.ui.visit.VisitEditScreen(visitId = null, onClose = { navController.popBackStack() }) }
        composable("visit/edit/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
            com.lessup.medledger.ui.visit.VisitEditScreen(visitId = id, onClose = { navController.popBackStack() })
        }
    }
}
