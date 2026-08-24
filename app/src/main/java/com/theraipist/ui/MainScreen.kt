package com.theraipist.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.theraipist.ui.chat.ChatScreen
import com.theraipist.ui.graph.GraphScreen
import com.theraipist.ui.newsession.NewSessionScreen
import com.theraipist.ui.sessions.SessionsScreen
import com.theraipist.ui.settings.SettingsScreen

/**
 * Tabbed shell behind the PIN gate, mirroring the iOS `RootTabView`.
 *
 * Chats is a session list rather than a chat, as on iOS: a conversation is
 * always entered from a session, and new ones are started through the New
 * Session flow where the persona is chosen. That is why there is no Persona
 * tab — persona belongs to a session, not to the app.
 */
private data class Tab(val route: String, val label: String)

private val TABS = listOf(
    Tab("chats", "Chats"),
    Tab("insights", "Insights"),
    Tab("settings", "Settings")
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var selected by remember { mutableStateOf("chats") }

    fun switchTo(route: String) {
        selected = route
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                TABS.forEach { tab ->
                    NavigationBarItem(
                        selected = selected == tab.route,
                        onClick = { switchTo(tab.route) },
                        label = { Text(tab.label) },
                        icon = {}
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "chats",
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            composable("chats") {
                SessionsScreen(
                    onOpenSession = { navController.navigate("chat") },
                    onNewSession = { navController.navigate("newSession") }
                )
            }
            composable("newSession") {
                NewSessionScreen(
                    onCreated = {
                        navController.navigate("chat") { popUpTo("chats") }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable("chat") {
                ChatScreen(
                    onOpenSessions = { navController.popBackStack("chats", inclusive = false) },
                    onOpenSettings = { switchTo("settings") }
                )
            }
            composable("insights") { GraphScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
