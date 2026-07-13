package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.FitViewModel
import com.example.data.FoodLog
import com.example.data.WorkoutLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FitViewModel,
    onNavigateToTracker: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val foodToday by viewModel.foodLogsForSelectedDate.collectAsStateWithLifecycle()
    val waterToday by viewModel.waterLogsForSelectedDate.collectAsStateWithLifecycle()
    val sleepToday by viewModel.sleepLogForSelectedDate.collectAsStateWithLifecycle()
    val workoutsToday by viewModel.workoutLogsForSelectedDate.collectAsStateWithLifecycle()

    val p = profile ?: return Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }

    // Calculations
    val targetCalories = viewModel.calculateTargetCalories(p).toInt()
    val consumedCalories = foodToday.sumOf { it.calories }
    val remainingCalories = (targetCalories - consumedCalories).coerceAtLeast(0)
    
    val targetProtein = (targetCalories * 0.3 / 4).toDouble()
    val targetCarbs = (targetCalories * 0.4 / 4).toDouble()
    val targetFat = (targetCalories * 0.3 / 9).toDouble()

    val consumedProtein = foodToday.sumOf { it.protein }
    val consumedCarbs = foodToday.sumOf { it.carbs }
    val consumedFat = foodToday.sumOf { it.fat }

    val totalWaterMl = waterToday.sumOf { it.amountMl }
    val waterTargetMl = (p.weight * 35).toInt() // scientifically backed
    
    val bmi = viewModel.calculateBmi(p)
    val bmiCat = viewModel.getBmiCategory(bmi)

    // Motivation quotes
    val quote = remember {
        val quotes = listOf(
            "“The only bad workout is the one that didn't happen.”",
            "“No citizen has a right to be an amateur in the matter of physical training.” — Socrates",
            "“Your body can stand almost anything. It's your mind that you have to convince.”",
            "“Success isn't always about greatness. It's about consistency.”",
            "“Energy flows where attention goes. Focus on your strength.”"
        )
        quotes[p.id % quotes.size]
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FitAI Coach",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Points",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Lvl ${p.level} • ${p.points} XP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Welcome
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Hello, ${p.firstName}! 👋",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Let's crush your goal of '${p.goal}' today!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.WorkspacePremium, null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Current Streak: ${p.streakDays} Days 🔥",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Calorie & Macro Target Progress Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Energy Balance",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("TARGET", style = MaterialTheme.typography.labelSmall)
                                Text("$targetCalories kcal", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$remainingCalories",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("kcal left", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("CONSUMED", style = MaterialTheme.typography.labelSmall)
                                Text("$consumedCalories kcal", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Macros Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MacroBar("Protein", consumedProtein.toInt(), targetProtein.toInt(), MaterialTheme.colorScheme.primary)
                            MacroBar("Carbs", consumedCarbs.toInt(), targetCarbs.toInt(), MaterialTheme.colorScheme.secondary)
                            MacroBar("Fats", consumedFat.toInt(), targetFat.toInt(), MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }

            // Quick Track Bar (Quick add shortcuts)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionBtn(
                        tag = "quick_water_btn",
                        icon = Icons.Default.LocalActivity,
                        label = "+250ml Water",
                        color = MaterialTheme.colorScheme.secondary
                    ) {
                        viewModel.addWater(250)
                    }
                    QuickActionBtn(
                        tag = "quick_snack_btn",
                        icon = Icons.Default.Restaurant,
                        label = "+Snack",
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        viewModel.addFood("Protein Snack", "Snack", 180, 15.0, 10.0, 4.0)
                    }
                    QuickActionBtn(
                        tag = "quick_workout_btn",
                        icon = Icons.Default.FitnessCenter,
                        label = "+Workout",
                        color = MaterialTheme.colorScheme.tertiary
                    ) {
                        viewModel.addWorkout("Custom Activity", 30, 220, "Home")
                    }
                }
            }

            // BMI, Sleep & Water Progress Block
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToTracker() },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WaterDrop, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Hydration", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("$totalWaterMl / $waterTargetMl ml", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = if (waterTargetMl > 0) (totalWaterMl.toFloat() / waterTargetMl).coerceAtMost(1f) else 0f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToTracker() },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonitorWeight, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Body BMI", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("${String.format("%.1f", bmi)} ($bmiCat)", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Weight: ${p.weight} kg", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            // Sleep Metric Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTracker() },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bedtime, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Sleep & Energy Log", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    text = if (sleepToday != null) "${sleepToday?.hours} hrs • Quality: ${sleepToday?.quality}" else "No sleep logged for today",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }

            // Today's Meals logged list
            if (foodToday.isNotEmpty()) {
                item {
                    Text("Today's Food Log", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                items(foodToday) { food ->
                    FoodLogItem(food, onDelete = { viewModel.deleteFood(food.id) })
                }
            }

            // Today's Workouts logged list
            if (workoutsToday.isNotEmpty()) {
                item {
                    Text("Completed Workouts", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                items(workoutsToday) { wk ->
                    WorkoutLogItem(wk, onDelete = { viewModel.deleteWorkout(wk.id) })
                }
            }

            // Daily Motivation Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Daily Motivation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = quote,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroBar(label: String, value: Int, target: Int, color: Color) {
    val progress = if (target > 0) (value.toFloat() / target).coerceAtMost(1f) else 0f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(8.dp)
                .background(color.copy(alpha = 0.2f), CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(color, CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("$value / $target g", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun QuickActionBtn(
    tag: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(105.dp)
            .testTag(tag)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun FoodLogItem(food: FoodLog, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(food.foodName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "${food.mealType} • P: ${food.protein.toInt()}g C: ${food.carbs.toInt()}g F: ${food.fat.toInt()}g",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${food.calories} kcal", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun WorkoutLogItem(workout: WorkoutLog, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FitnessCenter, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(workout.workoutName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${workout.durationMinutes} mins • ${workout.type}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("-${workout.caloriesBurned} kcal", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.tertiary)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
