package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.FitViewModel
import com.example.data.WeightLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerLogScreen(viewModel: FitViewModel) {
    val weightLogs by viewModel.weightLogs.collectAsStateWithLifecycle()
    val foodLogs by viewModel.foodLogsForSelectedDate.collectAsStateWithLifecycle()
    val waterLogs by viewModel.waterLogsForSelectedDate.collectAsStateWithLifecycle()
    val sleepLog by viewModel.sleepLogForSelectedDate.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("Weight") } // Weight, Food, Water, Sleep

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Logs & Analytics", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = when (activeTab) {
                    "Weight" -> 0
                    "Food" -> 1
                    "Water" -> 2
                    "Sleep" -> 3
                    else -> 0
                }
            ) {
                Tab(selected = activeTab == "Weight", onClick = { activeTab = "Weight" }) {
                    Text("Weight", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == "Food", onClick = { activeTab = "Food" }) {
                    Text("Food", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == "Water", onClick = { activeTab = "Water" }) {
                    Text("Water", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == "Sleep", onClick = { activeTab = "Sleep" }) {
                    Text("Sleep", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body depending on active log tab
            when (activeTab) {
                "Weight" -> WeightTrackerTab(viewModel, weightLogs)
                "Food" -> FoodTrackerTab(viewModel, foodLogs)
                "Water" -> WaterTrackerTab(viewModel, waterLogs)
                "Sleep" -> SleepTrackerTab(viewModel, sleepLog)
            }
        }
    }
}

// --- 1. WEIGHT TRACKER TAB ---
@Composable
fun WeightTrackerTab(viewModel: FitViewModel, logs: List<WeightLog>) {
    var weightInput by remember { mutableStateOf("") }
    var fatInput by remember { mutableStateOf("") }
    var waistInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Line chart progress
        if (logs.size >= 2) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📈 Weight Progress Trend (kg)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        WeightLineChart(logs = logs)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Showing history trend over your logged periods.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Add Log Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Log Today's Biometrics", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Current Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("log_weight_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fatInput,
                            onValueChange = { fatInput = it },
                            label = { Text("Body Fat %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = waistInput,
                            onValueChange = { waistInput = it },
                            label = { Text("Waist (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_weight_btn"),
                        onClick = {
                            val w = weightInput.toDoubleOrNull()
                            if (w != null) {
                                viewModel.logWeight(
                                    weight = w,
                                    bodyFat = fatInput.toDoubleOrNull() ?: 0.0,
                                    waist = waistInput.toDoubleOrNull() ?: 0.0
                                )
                                weightInput = ""
                                fatInput = ""
                                waistInput = ""
                            }
                        }
                    ) {
                        Text("Save Log")
                    }
                }
            }
        }

        // History logs list
        item {
            Text("Weight History Log", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (logs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No logs saved yet. Start by entering your weight above!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(logs.reversed()) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Weight: ${log.weight} kg", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Date: ${log.date}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (log.bodyFat > 0.0 || log.waist > 0.0) {
                            Text(
                                text = "Fat: ${log.bodyFat}% • Waist: ${log.waist}cm",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Custom responsive graph canvas
@Composable
fun WeightLineChart(logs: List<WeightLog>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color.Transparent)
    ) {
        val width = size.width
        val height = size.height
        val padding = 32f

        val xStep = (width - padding * 2) / (logs.size - 1).coerceAtLeast(1)
        
        val weights = logs.map { it.weight.toFloat() }
        val minWeight = weights.minOrNull() ?: 0f
        val maxWeight = weights.maxOrNull() ?: 100f
        val weightRange = (maxWeight - minWeight).coerceAtLeast(1f)

        val points = logs.mapIndexed { index, log ->
            val x = padding + index * xStep
            // Invert Y axis
            val y = height - padding - ((log.weight.toFloat() - minWeight) / weightRange) * (height - padding * 2)
            Offset(x, y)
        }

        // Draw grids
        drawLine(
            color = labelColor.copy(alpha = 0.2f),
            start = Offset(padding, height - padding),
            end = Offset(width - padding, height - padding),
            strokeWidth = 2f
        )

        // Draw Graph lines & path
        val strokePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
        }

        drawPath(
            path = strokePath,
            color = primaryColor,
            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw glow fill under line
        val fillPath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, height - padding)
                for (pt in points) {
                    lineTo(pt.x, pt.y)
                }
                lineTo(points.last().x, height - padding)
                close()
            }
        }
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                startY = points.map { it.y }.minOrNull() ?: 0f,
                endY = height - padding
            )
        )

        // Draw node points
        for (pt in points) {
            drawCircle(color = primaryColor, radius = 8f, center = pt)
            drawCircle(color = Color.White, radius = 4f, center = pt)
        }
    }
}

// --- 2. FOOD TRACKER TAB ---
@Composable
fun FoodTrackerTab(viewModel: FitViewModel, foodLogs: List<com.example.data.FoodLog>) {
    var nameInput by remember { mutableStateOf("") }
    var calInput by remember { mutableStateOf("") }
    var protInput by remember { mutableStateOf("") }
    var carbInput by remember { mutableStateOf("") }
    var fatInput by remember { mutableStateOf("") }
    var mealTypeSelected by remember { mutableStateOf("Breakfast") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Food Item", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { meal ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (mealTypeSelected == meal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { mealTypeSelected = meal }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = meal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mealTypeSelected == meal) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Food Item Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = calInput,
                            onValueChange = { calInput = it },
                            label = { Text("Calories") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = protInput,
                            onValueChange = { protInput = it },
                            label = { Text("Protein (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = carbInput,
                            onValueChange = { carbInput = it },
                            label = { Text("Carbs (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = fatInput,
                            onValueChange = { fatInput = it },
                            label = { Text("Fats (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val c = calInput.toIntOrNull()
                            if (nameInput.isNotEmpty() && c != null) {
                                viewModel.addFood(
                                    name = nameInput,
                                    mealType = mealTypeSelected,
                                    calories = c,
                                    protein = protInput.toDoubleOrNull() ?: 0.0,
                                    carbs = carbInput.toDoubleOrNull() ?: 0.0,
                                    fat = fatInput.toDoubleOrNull() ?: 0.0
                                )
                                nameInput = ""
                                calInput = ""
                                protInput = ""
                                carbInput = ""
                                fatInput = ""
                            }
                        }
                    ) {
                        Text("Add Food")
                    }
                }
            }
        }

        item {
            Text("Logged Today", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (foodLogs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No food logged today.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(foodLogs) { food ->
                FoodLogItem(food, onDelete = { viewModel.deleteFood(food.id) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- 3. WATER TRACKER TAB ---
@Composable
fun WaterTrackerTab(viewModel: FitViewModel, waterLogs: List<com.example.data.WaterLog>) {
    val totalWater = waterLogs.sumOf { it.amountMl }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Water",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Total Water Drank Today", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$totalWater ml", fontWeight = FontWeight.Black, fontSize = 36.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { viewModel.addWater(250) }) {
                            Text("+250 ml")
                        }
                        Button(onClick = { viewModel.addWater(500) }) {
                            Text("+500 ml")
                        }
                        Button(onClick = { viewModel.addWater(750) }) {
                            Text("+750 ml")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- 4. SLEEP TRACKER TAB ---
@Composable
fun SleepTrackerTab(viewModel: FitViewModel, sleepLog: com.example.data.SleepLog?) {
    var hoursInput by remember { mutableStateOf("") }
    var selectedQuality by remember { mutableStateOf("Restful") }
    var moodInput by remember { mutableStateOf("Focused") }
    var energyInput by remember { mutableStateOf("High") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Log Sleep Metrics", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = hoursInput,
                        onValueChange = { hoursInput = it },
                        label = { Text("Hours Slept") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Sleep Quality:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Restful", "Normal", "Disturbed").forEach { q ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (selectedQuality == q) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedQuality = q }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = q,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedQuality == q) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = moodInput,
                        onValueChange = { moodInput = it },
                        label = { Text("How was your mood? (e.g. Focused, Anxious, Calm)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = energyInput,
                        onValueChange = { energyInput = it },
                        label = { Text("Daily Energy levels? (e.g. High, Medium, Low)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val h = hoursInput.toDoubleOrNull()
                            if (h != null) {
                                viewModel.addSleep(
                                    hours = h,
                                    quality = selectedQuality,
                                    mood = moodInput,
                                    energyLevel = energyInput
                                )
                                hoursInput = ""
                            }
                        }
                    ) {
                        Text("Save Sleep Log")
                    }
                }
            }
        }

        if (sleepLog != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Logged Sleep for Today:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Duration: ${sleepLog.hours} hours", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("• Quality: ${sleepLog.quality}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("• Mood: ${sleepLog.mood}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("• Energy Level: ${sleepLog.energyLevel}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
