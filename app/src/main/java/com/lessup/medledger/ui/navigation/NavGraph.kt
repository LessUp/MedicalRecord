package com.lessup.medledger.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lessup.medledger.ui.home.HomeScreen
import com.lessup.medledger.ui.chronic.ChronicScreen
import com.lessup.medledger.ui.settings.SettingsScreen
import com.lessup.medledger.ui.calendar.CalendarScreen
import com.lessup.medledger.ui.auth.LoginScreen
import com.lessup.medledger.ui.profile.ProfileScreen
import com.lessup.medledger.ui.family.FamilyScreen

sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Dest("home", "首页", Icons.Outlined.Home)
    data object Chronic : Dest("chronic", "慢病", Icons.Outlined.FavoriteBorder)
    data object Profile : Dest("profile", "我的", Icons.Outlined.Person)
    data object Settings : Dest("settings", "设置", Icons.Outlined.Settings)
}

val bottomDestinations = listOf(Dest.Home, Dest.Chronic, Dest.Profile)

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Dest.Home.route) {
        // 主页面
        composable(Dest.Home.route) {
            HomeScreen(
                onEdit = { id -> if (id == null) navController.navigate("visit/edit") else navController.navigate("visit/detail/$id") },
                onScan = { navController.navigate("scan") },
                onCalendar = { navController.navigate("calendar") }
            )
        }
        composable(Dest.Chronic.route) { ChronicScreen() }
        composable(Dest.Profile.route) {
            ProfileScreen(
                onLogin = { navController.navigate("login") },
                onFamilyMembers = { navController.navigate("family") },
                onSettings = { navController.navigate(Dest.Settings.route) }
            )
        }
        composable(Dest.Settings.route) { SettingsScreen() }
        
        // 登录页面
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.popBackStack() },
                onSkip = { navController.popBackStack() }
            )
        }
        
        // 家庭成员
        composable("family") {
            FamilyScreen(
                onBack = { navController.popBackStack() },
                onMemberClick = { /* TODO */ }
            )
        }
        
        // 就诊记录
        composable("visit/edit") { 
            com.lessup.medledger.ui.visit.VisitEditScreen(
                visitId = null, 
                onClose = { navController.popBackStack() }
            ) 
        }
        composable("visit/edit/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
            com.lessup.medledger.ui.visit.VisitEditScreen(visitId = id, onClose = { navController.popBackStack() })
        }
        composable("visit/detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
            if (id != null) {
                com.lessup.medledger.ui.visit.VisitDetailScreen(
                    visitId = id,
                    onClose = { navController.popBackStack() },
                    onEdit = { navController.navigate("visit/edit/$id") },
                    onScan = { navController.navigate("scan/$id") }
                )
            }
        }
        
        // 扫描
        composable("scan") { 
            com.lessup.medledger.ui.scan.ScanScreen(
                visitId = null, 
                onClose = { navController.popBackStack() }
            ) 
        }
        composable("scan/{visitId}") { backStackEntry ->
            val vid = backStackEntry.arguments?.getString("visitId")?.toLongOrNull()
            com.lessup.medledger.ui.scan.ScanScreen(visitId = vid, onClose = { navController.popBackStack() })
        }
        
        // 日历
        composable("calendar") {
            CalendarScreen(
                onClose = { navController.popBackStack() },
                onVisitClick = { id -> navController.navigate("visit/detail/$id") }
            )
        }
        
        // 费用统计
        composable("stats") {
            com.lessup.medledger.ui.stats.StatsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
