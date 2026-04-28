package com.freelink.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.freelink.core.ui.FreeLinkPlaceholderScreen
import com.freelink.feature.auth.ui.AuthRoute
import com.freelink.feature.chatlist.ui.ChatListRoute
import com.freelink.feature.devices.ui.DeviceSessionsRoute
import com.freelink.feature.profile.ui.ProfileRoute
import com.freelink.feature.settings.ui.SettingsRoute

private data class RootDestination(
    val route: String,
    val label: String
)

private val rootDestinations = listOf(
    RootDestination(route = "chats", label = "Chats"),
    RootDestination(route = "people", label = "People"),
    RootDestination(route = "spaces", label = "Spaces"),
    RootDestination(route = "calls", label = "Calls"),
    RootDestination(route = "profile", label = "Profile")
)

private const val authRoute = "auth"
private const val settingsRoute = "settings"
private const val devicesRoute = "devices"

@Composable
fun FreeLinkAppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = rootDestinations.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                FreeLinkBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = authRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = authRoute) {
                AuthRoute(
                    onAuthorized = {
                        navController.navigate("chats") {
                            popUpTo(authRoute) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(route = "chats") { ChatListRoute() }
            composable(route = "people") { FreeLinkPlaceholderScreen(title = "People") }
            composable(route = "spaces") { FreeLinkPlaceholderScreen(title = "Spaces") }
            composable(route = "calls") { FreeLinkPlaceholderScreen(title = "Calls") }
            composable(route = "profile") {
                ProfileRoute(
                    onOpenPrivacySettings = {
                        navController.navigate(settingsRoute) {
                            launchSingleTop = true
                        }
                    },
                    onOpenDevices = {
                        navController.navigate(devicesRoute) {
                            launchSingleTop = true
                        }
                    },
                    onLoggedOut = {
                        navController.navigate(authRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(route = settingsRoute) {
                SettingsRoute(
                    onOpenDevices = {
                        navController.navigate(devicesRoute) {
                            launchSingleTop = true
                        }
                    },
                    onLoggedOut = {
                        navController.navigate(authRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(route = devicesRoute) {
                DeviceSessionsRoute(
                    onLoggedOut = {
                        navController.navigate(authRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FreeLinkBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        rootDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Text(text = destination.label.take(1)) },
                label = { Text(text = destination.label) }
            )
        }
    }
}
