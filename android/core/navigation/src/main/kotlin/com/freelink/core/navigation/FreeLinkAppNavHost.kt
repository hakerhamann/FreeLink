package com.freelink.core.navigation

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.freelink.core.ui.FreeLinkPlaceholderScreen
import com.freelink.feature.archive.ui.ArchiveRoute
import com.freelink.feature.auth.ui.AuthRoute
import com.freelink.feature.chat.ui.DirectChatRoute
import com.freelink.feature.chatlist.ui.ChatListRoute
import com.freelink.feature.devices.ui.DeviceSessionsRoute
import com.freelink.feature.group.ui.GroupRoute
import com.freelink.feature.media_gallery.ui.MediaGalleryRoute
import com.freelink.feature.people.ui.PeopleRoute
import com.freelink.feature.profile.ui.ProfileRoute
import com.freelink.feature.settings.ui.SettingsRoute

private data class RootDestination(
    val route: String,
    val label: String
)

private val rootDestinations = listOf(
    RootDestination(route = "chats", label = "Чаты"),
    RootDestination(route = "people", label = "Люди"),
    RootDestination(route = "spaces", label = "Группы"),
    RootDestination(route = "calls", label = "Звонки"),
    RootDestination(route = "profile", label = "Профиль")
)

private const val authRoute = "auth"
private const val directChatRoute = "chat/{chatId}?title={chatTitle}"
private const val settingsRoute = "settings"
private const val devicesRoute = "devices"
private const val mediaGalleryRoute = "media-gallery"
private const val archiveRoute = "archive"

@Composable
fun FreeLinkAppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = rootDestinations.any { it.route == currentRoute }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                            popUpTo(authRoute) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(route = "chats") {
                ChatListRoute { chatId, chatTitle ->
                    navController.navigate("chat/${Uri.encode(chatId)}?title=${Uri.encode(chatTitle)}") {
                        launchSingleTop = true
                    }
                }
            }
            composable(route = directChatRoute) { backStackEntry ->
                DirectChatRoute(
                    chatId = backStackEntry.arguments?.getString("chatId").orEmpty(),
                    chatTitle = backStackEntry.arguments?.getString("chatTitle").orEmpty(),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(route = "people") { PeopleRoute() }
            composable(route = "spaces") {
                GroupRoute { groupId, groupTitle ->
                    navController.navigate("chat/${Uri.encode(groupId)}?title=${Uri.encode(groupTitle)}") {
                        launchSingleTop = true
                    }
                }
            }
            composable(route = "calls") { FreeLinkPlaceholderScreen(title = "Звонки") }
            composable(route = "profile") {
                ProfileRoute(
                    onOpenPrivacySettings = { navController.navigate(settingsRoute) { launchSingleTop = true } },
                    onOpenDevices = { navController.navigate(devicesRoute) { launchSingleTop = true } },
                    onOpenMediaGallery = { navController.navigate(mediaGalleryRoute) { launchSingleTop = true } },
                    onLoggedOut = {
                        navController.navigate(authRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(route = mediaGalleryRoute) { MediaGalleryRoute(onBack = { navController.popBackStack() }) }
            composable(route = settingsRoute) {
                SettingsRoute(
                    onOpenDevices = { navController.navigate(devicesRoute) { launchSingleTop = true } },
                    onOpenArchive = { navController.navigate(archiveRoute) { launchSingleTop = true } },
                    onLoggedOut = {
                        navController.navigate(authRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(route = archiveRoute) { ArchiveRoute(onBack = { navController.popBackStack() }) }
            composable(route = devicesRoute) {
                DeviceSessionsRoute(
                    onLoggedOut = {
                        navController.navigate(authRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
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

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 0.dp
    ) {
        rootDestinations.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { FreeLinkBottomIcon(route = destination.route, selected = selected) },
                label = { Text(text = destination.label, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
            )
        }
    }
}

@Composable
private fun FreeLinkBottomIcon(route: String, selected: Boolean) {
    val accent = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    val glow = MaterialTheme.colorScheme.secondary.copy(alpha = if (selected) 0.28f else 0.0f)
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(
                brush = Brush.radialGradient(listOf(glow, Color.Transparent)),
                shape = RoundedCornerShape(15.dp)
            )
    ) {
        Canvas(modifier = Modifier.size(30.dp)) {
            val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
            when (route) {
                "chats" -> {
                    drawRoundRect(accent, topLeft = Offset(5.dp.toPx(), 7.dp.toPx()), size = Size(20.dp.toPx(), 14.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(7.dp.toPx()), style = stroke)
                    drawLine(accent, Offset(11.dp.toPx(), 21.dp.toPx()), Offset(8.dp.toPx(), 25.dp.toPx()), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
                }
                "people" -> {
                    drawCircle(accent, radius = 4.dp.toPx(), center = Offset(11.dp.toPx(), 10.dp.toPx()), style = stroke)
                    drawCircle(accent, radius = 4.dp.toPx(), center = Offset(19.dp.toPx(), 10.dp.toPx()), style = stroke)
                    drawArc(accent, 205f, 130f, false, topLeft = Offset(6.dp.toPx(), 15.dp.toPx()), size = Size(18.dp.toPx(), 12.dp.toPx()), style = stroke)
                }
                "spaces" -> {
                    drawRoundRect(accent, topLeft = Offset(8.dp.toPx(), 6.dp.toPx()), size = Size(14.dp.toPx(), 14.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()), style = stroke)
                    drawLine(accent, Offset(15.dp.toPx(), 4.dp.toPx()), Offset(24.dp.toPx(), 11.dp.toPx()), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(accent, Offset(15.dp.toPx(), 26.dp.toPx()), Offset(6.dp.toPx(), 19.dp.toPx()), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
                }
                "calls" -> {
                    drawArc(accent, 130f, 280f, false, topLeft = Offset(7.dp.toPx(), 5.dp.toPx()), size = Size(17.dp.toPx(), 20.dp.toPx()), style = stroke)
                    drawLine(accent, Offset(9.dp.toPx(), 20.dp.toPx()), Offset(13.dp.toPx(), 24.dp.toPx()), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
                }
                else -> {
                    drawCircle(accent, radius = 6.dp.toPx(), center = Offset(15.dp.toPx(), 10.dp.toPx()), style = stroke)
                    drawArc(accent, 205f, 130f, false, topLeft = Offset(6.dp.toPx(), 15.dp.toPx()), size = Size(18.dp.toPx(), 12.dp.toPx()), style = stroke)
                }
            }
        }
    }
}
