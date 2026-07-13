package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FitViewModel
import com.example.ui.screens.*

sealed class NavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : NavItem("home", "Home", Icons.Default.Home)
    object Coach : NavItem("coach", "Coach AI", Icons.Default.Chat)
    object Planners : NavItem("planners", "Planners", Icons.Default.FitnessCenter)
    object Logs : NavItem("logs", "Log Tracker", Icons.Default.TrendingUp)
    object SettingsHub : NavItem("settings", "Wellness", Icons.Default.Menu)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationContainer(
    viewModel: FitViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    var selectedItem by remember { mutableStateOf<NavItem>(NavItem.Home) }
    
    // Sub-navigation state for Planners Tab
    var plannerSubTab by remember { mutableStateOf("Diet") } // Diet, Workout

    // Sub-navigation state for Wellness Hub
    var wellnessSubTab by remember { mutableStateOf("Profile") } // Profile, Achievements, Recipes, Settings

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_navigation_bar"),
                tonalElevation = 8.dp
            ) {
                val items = listOf(
                    NavItem.Home,
                    NavItem.Coach,
                    NavItem.Planners,
                    NavItem.Logs,
                    NavItem.SettingsHub
                )
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 11.sp) },
                        selected = selectedItem == item,
                        onClick = { selectedItem = item },
                        modifier = Modifier.testTag("nav_item_${item.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedItem) {
                NavItem.Home -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTracker = { selectedItem = NavItem.Logs },
                        onNavigateToChat = { selectedItem = NavItem.Coach }
                    )
                }
                NavItem.Coach -> {
                    AICoachChatScreen(viewModel = viewModel)
                }
                NavItem.Planners -> {
                    // Planners hub with a top sub-tab Row
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = if (plannerSubTab == "Diet") 0 else 1,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Tab(selected = plannerSubTab == "Diet", onClick = { plannerSubTab = "Diet" }) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Icon(Icons.Default.RestaurantMenu, null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Diet Plan", fontWeight = FontWeight.Bold)
                                }
                            }
                            Tab(selected = plannerSubTab == "Workout", onClick = { plannerSubTab = "Workout" }) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Icon(Icons.Default.FitnessCenter, null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Workout split", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Box(modifier = Modifier.weight(1f)) {
                            if (plannerSubTab == "Diet") {
                                DietPlanScreen(viewModel = viewModel)
                            } else {
                                WorkoutPlanScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
                NavItem.Logs -> {
                    TrackerLogScreen(viewModel = viewModel)
                }
                NavItem.SettingsHub -> {
                    // Sub tabbed layout for Settings / Hub items
                    Column(modifier = Modifier.fillMaxSize()) {
                        ScrollableTabRow(
                            selectedTabIndex = when (wellnessSubTab) {
                                "Profile" -> 0
                                "Recipes" -> 1
                                "Badges" -> 2
                                "Settings" -> 3
                                else -> 0
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Tab(selected = wellnessSubTab == "Profile", onClick = { wellnessSubTab = "Profile" }) {
                                Text("Biometrics Profile", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Tab(selected = wellnessSubTab == "Recipes", onClick = { wellnessSubTab = "Recipes" }) {
                                Text("Recipes", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Tab(selected = wellnessSubTab == "Badges", onClick = { wellnessSubTab = "Badges" }) {
                                Text("Achievements", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Tab(selected = wellnessSubTab == "Settings", onClick = { wellnessSubTab = "Settings" }) {
                                Text("Settings", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (wellnessSubTab) {
                                "Profile" -> ProfileSetupScreen(viewModel = viewModel)
                                "Recipes" -> RecipeSectionScreen(viewModel = viewModel)
                                "Badges" -> GamificationScreen(viewModel = viewModel)
                                "Settings" -> SettingsScreen(
                                    viewModel = viewModel,
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = onToggleTheme
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
