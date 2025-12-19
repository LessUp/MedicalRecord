package com.lessup.medledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lessup.medledger.ui.theme.AppTheme
import com.lessup.medledger.ui.navigation.AppNavHost
import com.lessup.medledger.ui.navigation.bottomDestinations
import com.lessup.medledger.ui.navigation.Dest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                // 只在主要页面显示底部导航和FAB
                val isMainScreen = currentRoute in listOf(
                    Dest.Home.route,
                    Dest.Chronic.route,
                    Dest.Profile.route,
                    Dest.Settings.route
                )
                val showFab = currentRoute == Dest.Home.route

                Scaffold(
                    floatingActionButton = {
                        AnimatedVisibility(
                            visible = showFab,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            FloatingActionButton(
                                onClick = { navController.navigate("visit/edit") },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = "新增就诊")
                            }
                        }
                    },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = isMainScreen,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            NavigationBar {
                                bottomDestinations.forEach { dest ->
                                    NavigationBarItem(
                                        selected = currentRoute == dest.route,
                                        onClick = {
                                            if (currentRoute != dest.route) {
                                                navController.navigate(dest.route) {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                                        label = { Text(dest.label) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            AppNavHost(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
