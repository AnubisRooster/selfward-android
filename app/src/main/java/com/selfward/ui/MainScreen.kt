package com.selfward.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.selfward.ui.chat.ChatScreen
import com.selfward.ui.graph.GraphScreen
import com.selfward.ui.journal.JournalScreen
import com.selfward.ui.narrative.NarrativeScreen
import com.selfward.ui.newsession.NewSessionScreen
import com.selfward.ui.sessions.SessionsScreen
import com.selfward.ui.settings.SettingsScreen

/**
 * Tabbed shell behind the PIN gate, mirroring the iOS `RootTabView`.
 *
 * Chats is a session list rather than a chat, as on iOS: a conversation is
 * always entered from a session, and new ones are started through the New
 * Session flow where the persona is chosen. That is why there is no Persona
 * tab — persona belongs to a session, not to the app.
 */
private data class Tab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    /** Filled variant shown for the active tab; outlined otherwise. */
    val selectedIcon: ImageVector
)

private val TABS = listOf(
    Tab("chats", "Chats", Icons.Outlined.Forum, Icons.Filled.Forum),
    Tab("narrative", "Narrative", Icons.Outlined.MenuBook, Icons.Filled.MenuBook),
    Tab("journal", "Journal", Icons.Outlined.EditNote, Icons.Filled.EditNote),
    Tab("insights", "Insights", Icons.Outlined.Insights, Icons.Filled.Insights),
    Tab("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
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
                    val isSelected = selected == tab.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { switchTo(tab.route) },
                        label = { Text(tab.label) },
                        icon = {
                            Icon(
                                if (isSelected) tab.selectedIcon else tab.icon,
                                contentDescription = null
                            )
                        }
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
            composable("narrative") { NarrativeScreen() }
            composable("journal") { JournalScreen() }
            composable("insights") { GraphScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
