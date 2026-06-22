package com.aditya.simgateway.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aditya.simgateway.presentation.dashboard.DashboardScreen
import com.aditya.simgateway.presentation.device.DeviceScreen
import com.aditya.simgateway.presentation.logs.LogsScreen
import com.aditya.simgateway.presentation.messages.MessageDetailsScreen
import com.aditya.simgateway.presentation.messages.MessagesScreen
import com.aditya.simgateway.presentation.messages.TestSmsScreen
import com.aditya.simgateway.presentation.settings.SettingsScreen
import com.aditya.simgateway.ui.theme.DarkCard
import com.aditya.simgateway.ui.theme.DarkSurface
import com.aditya.simgateway.ui.theme.TextSecondary

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    data object Messages : Screen("messages", "Messages", Icons.Default.Sms)
    data object Logs : Screen("logs", "Logs", Icons.AutoMirrored.Filled.Article)
    data object Device : Screen("device", "Device", Icons.Default.Phone)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

private val screens = listOf(
    Screen.Dashboard,
    Screen.Messages,
    Screen.Logs,
    Screen.Device,
    Screen.Settings
)

@Composable
fun SimGatewayNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 0.dp
            ) {
                screens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == screen.route
                    } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(text = screen.title)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = DarkCard
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen()
            }
            composable(Screen.Messages.route) {
                MessagesScreen(
                    onOpenMessageDetails = { messageId ->
                        navController.navigate("messageDetails/${Uri.encode(messageId)}")
                    },
                    onOpenTestSms = {
                        navController.navigate("testSms")
                    }
                )
            }
            composable(Screen.Logs.route) {
                LogsScreen()
            }
            composable(Screen.Device.route) {
                DeviceScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable("messageDetails/{messageId}") { backStackEntry ->
                val messageId = backStackEntry.arguments?.getString("messageId").orEmpty()
                MessageDetailsScreen(
                    messageId = messageId,
                    onBack = navController::popBackStack
                )
            }
            composable("testSms") {
                TestSmsScreen(
                    onBack = navController::popBackStack
                )
            }
        }
    }
}
