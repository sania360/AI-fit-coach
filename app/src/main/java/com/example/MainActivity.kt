package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainNavigationContainer
import com.example.ui.theme.FitAICoachTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Manage dark theme toggle dynamically
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            FitAICoachTheme(darkTheme = isDarkTheme) {
                // Initialize FitViewModel using Compose factory
                val viewModel: FitViewModel = viewModel()
                
                MainNavigationContainer(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = it }
                )
            }
        }
    }
}
