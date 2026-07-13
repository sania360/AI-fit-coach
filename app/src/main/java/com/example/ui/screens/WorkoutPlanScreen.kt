package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.FitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlanScreen(viewModel: FitViewModel) {
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingWorkouts.collectAsStateWithLifecycle()
    val workoutPlan by viewModel.workoutPlanResult.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val p = profileState ?: return Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }

    var selectedExperience by remember { mutableStateOf(p.workoutExperience) }
    var selectedLocation by remember { mutableStateOf(p.gymAvailability) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Workout Generator", fontWeight = FontWeight.Bold) },
                actions = {
                    if (workoutPlan.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "My FitAI Coach Workout Plan")
                                    putExtra(Intent.EXTRA_TEXT, workoutPlan)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Workout Plan via"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Plan")
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
            // Setup parameters
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tailor Your Active Strategy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AI develops custom splits matching your available equipment (${p.equipmentAvailable}) and goal.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Experience Level Selection
                        Text("Workout Experience:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Beginner", "Intermediate", "Advanced").forEach { exp ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (selectedExperience == exp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedExperience = exp }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = exp,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedExperience == exp) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Training Location Selection
                        Text("Preferred Location:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Home", "Gym", "Both").forEach { loc ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (selectedLocation == loc) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedLocation = loc }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = loc,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedLocation == loc) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_workout_btn"),
                            onClick = {
                                viewModel.updateProfile(p.copy(workoutExperience = selectedExperience, gymAvailability = selectedLocation))
                                viewModel.requestWorkoutPlan()
                            },
                            enabled = !isGenerating,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Building routine...")
                            } else {
                                Icon(Icons.Default.FitnessCenter, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate 7-Day Workout split")
                            }
                        }
                    }
                }
            }

            // Results Section
            if (isGenerating) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(strokeWidth = 4.dp, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Coach FitAI is engineering your custom program...",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Assembling warmups, reps, sets and safety details.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            } else if (workoutPlan.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏋️ Your Weekly Fitness Split",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Text(
                                        text = p.goal,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))

                            // Custom native markdown renderer
                            MarkdownText(workoutPlan)
                        }
                    }
                }
            } else {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Workout Plan Active",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Click generate above to build a custom weekly routine matching your available equipment (${p.equipmentAvailable}) and goal.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
