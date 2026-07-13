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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.FitViewModel
import com.example.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietPlanScreen(viewModel: FitViewModel) {
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingMeals.collectAsStateWithLifecycle()
    val mealPlan by viewModel.mealPlanResult.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val p = profileState ?: return Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }

    var selectedDietStyle by remember { mutableStateOf(p.foodPreferences) }
    
    val targetCalories = viewModel.calculateTargetCalories(p).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Diet & Meal Planner", fontWeight = FontWeight.Bold) },
                actions = {
                    if (mealPlan.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "My FitAI Coach Diet Plan")
                                    putExtra(Intent.EXTRA_TEXT, mealPlan)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Diet Plan via"))
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
            // Options Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Customize Your Diet Plan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Our AI creates a 7-day personalized menu scientifically aligned with your daily goal of $targetCalories kcal.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Select Culinary Diet Style:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom grid of culinary styles
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val rows = listOf(
                                listOf("Mediterranean", "American Diet"),
                                listOf("Pakistani Diet", "Indian Diet"),
                                listOf("Keto / Low Carb", "Vegetarian / Vegan")
                            )
                            rows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { diet ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(
                                                    if (selectedDietStyle.contains(diet, ignoreCase = true)) {
                                                        MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                                    },
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    selectedDietStyle = diet
                                                }
                                                .padding(vertical = 10.dp, horizontal = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = diet,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedDietStyle.contains(diet, ignoreCase = true)) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSecondaryContainer
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_diet_btn"),
                            onClick = {
                                // Update profile's selected pref then request plan
                                viewModel.updateProfile(p.copy(foodPreferences = selectedDietStyle))
                                viewModel.requestMealPlan()
                            },
                            enabled = !isGenerating,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Chef is crafting plan...")
                            } else {
                                Icon(Icons.Default.RestaurantMenu, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate 7-Day Diet Plan")
                            }
                        }
                    }
                }
            }

            // Results Rendering Section
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
                            "Chef FitAI is preparing a scientific $selectedDietStyle meal plan...",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Calculating protein splits and grocery list.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            } else if (mealPlan.isNotEmpty()) {
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
                                    text = "🍳 Your Weekly Nutrition Plan",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Text(
                                        text = "$targetCalories kcal / day",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))

                            // Beautiful text display with custom formatting
                            MarkdownText(mealPlan)
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
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Diet Plan Active",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Click generate above to build a custom diet utilizing sports science algorithms and your biometric data.",
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

/**
 * A fast, clean native text parser to render headers, bullet lists, and normal lines nicely
 */
@Composable
fun MarkdownText(text: String) {
    val lines = text.split("\n")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("###") -> {
                    Text(
                        text = trimmed.removePrefix("###").trim().removePrefix("**").removeSuffix("**"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("##") -> {
                    Text(
                        text = trimmed.removePrefix("##").trim().removePrefix("**").removeSuffix("**"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                    )
                }
                trimmed.startsWith("#") -> {
                    Text(
                        text = trimmed.removePrefix("#").trim().removePrefix("**").removeSuffix("**"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                trimmed.startsWith("*") || trimmed.startsWith("-") -> {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = trimmed.substring(1).trim(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                else -> {
                    Text(
                        text = trimmed,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
