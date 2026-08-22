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
import com.theraipist.ui.persona.PersonaScreen
import com.theraipist.ui.settings.SettingsScreen

private val ROUTES = listOf("chat", "persona", "graph", "settings")

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var selected by remember { mutableStateOf("chat") }

    Scaffold(
        bottomBar = {
            NavigationBar {
                ROUTES.forEach { route ->
                    NavigationBarItem(
                        selected = selected == route,
                        onClick = {
                            selected = route
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(route.replaceFirstChar { it.uppercase() }) },
                        icon = {}
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "chat",
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            composable("chat") { ChatScreen() }
            composable("persona") { PersonaScreen() }
            composable("graph") { GraphScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
