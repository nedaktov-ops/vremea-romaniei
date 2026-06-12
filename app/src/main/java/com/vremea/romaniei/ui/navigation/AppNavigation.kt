package com.vremea.romaniei.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vremea.romaniei.ui.screens.alerts.AlertsScreen
import com.vremea.romaniei.ui.screens.forecast.ForecastScreen
import com.vremea.romaniei.ui.screens.home.HomeScreen
import com.vremea.romaniei.ui.screens.map.MapScreen
import com.vremea.romaniei.ui.screens.settings.SettingsScreen

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen("home", "Acasă", Icons.Filled.Cloud, Icons.Outlined.Cloud)
    data object Forecast : Screen("forecast", "Prognoză", Icons.Filled.DateRange, Icons.Outlined.DateRange)
    data object Map : Screen("map", "Hartă", Icons.Filled.Map, Icons.Outlined.Map)
    data object Alerts : Screen("alerts", "Alerte", Icons.Filled.Notifications, Icons.Outlined.Notifications)
    data object Settings : Screen("settings", "Setări", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Forecast,
    Screen.Map,
    Screen.Alerts,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Forecast.route) { ForecastScreen() }
            composable(Screen.Map.route) { MapScreen() }
            composable(Screen.Alerts.route) { AlertsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
