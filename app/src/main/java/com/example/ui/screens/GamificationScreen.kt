package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.FitViewModel

data class Badge(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val xpRequired: Int,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamificationScreen(viewModel: FitViewModel) {
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val p = profileState ?: return Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }

    // Points calculation to next level
    val baseLevelXp = (p.level - 1) * 500
    val currentLevelXp = p.points - baseLevelXp
    val xpNeededForNext = 500
    val progress = (currentLevelXp.toFloat() / xpNeededForNext).coerceIn(0f, 1f)

    val badges = listOf(
        Badge("First Step", "Started your premium fitness journey with FitAI.", Icons.Default.DirectionsRun, 100, MaterialTheme.colorScheme.primary),
        Badge("Hydration Hero", "Drank and logged water 3 times today.", Icons.Default.WaterDrop, 150, MaterialTheme.colorScheme.secondary),
        Badge("Calorie Master", "Logged your breakfasts, lunches, and dinners.", Icons.Default.LocalPizza, 250, MaterialTheme.colorScheme.primary),
        Badge("Early Bird", "Slept restful 7.5 hours and logged sleep quality.", Icons.Default.Bedtime, 300, MaterialTheme.colorScheme.tertiary),
        Badge("Iron Titan", "Completed a lower or upper hypertrophy split.", Icons.Default.FitnessCenter, 500, MaterialTheme.colorScheme.error),
        Badge("Streak Beast", "Reached a active streak of 3+ consecutive days.", Icons.Default.LocalFireDepartment, 800, MaterialTheme.colorScheme.tertiary)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Level & Accomplishments", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Level progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${p.level}",
                            fontWeight = FontWeight.Black,
                            fontSize = 36.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Level ${p.level} Athlete", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Current XP: ${p.points} XP • Streak: ${p.streakDays} Days 🔥", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("XP progress: $currentLevelXp / $xpNeededForNext XP", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Level ${p.level + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Accomplishments & Badges", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Badges Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(badges) { badge ->
                    val unlocked = p.points >= badge.xpRequired
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (unlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        if (unlocked) badge.color.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = badge.icon,
                                    contentDescription = null,
                                    tint = if (unlocked) badge.color else Color.Gray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = badge.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (unlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = badge.description,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (unlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = if (unlocked) "UNLOCKED" else "${badge.xpRequired} XP req",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unlocked) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
