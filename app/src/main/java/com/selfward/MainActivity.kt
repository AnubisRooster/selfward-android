package com.selfward

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.selfward.ui.AppRoot
import com.selfward.ui.theme.SelfwardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 15 draws apps behind the system bars whatever they ask for, and
        // ignores window.statusBarColor entirely. Opting in here rather than
        // being opted in means the bars get a colour that matches the theme
        // instead of whatever the platform picks.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SelfwardTheme { AppRoot() }
        }
    }
}
