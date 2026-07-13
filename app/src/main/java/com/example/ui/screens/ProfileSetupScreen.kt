package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.FitViewModel
import com.example.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(viewModel: FitViewModel) {
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = profileState ?: return Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }

    // Editable States
    var firstName by remember(profile) { mutableStateOf(profile.firstName) }
    var lastName by remember(profile) { mutableStateOf(profile.lastName) }
    var email by remember(profile) { mutableStateOf(profile.email) }
    var gender by remember(profile) { mutableStateOf(profile.gender) }
    var age by remember(profile) { mutableStateOf(profile.age.toString()) }
    var country by remember(profile) { mutableStateOf(profile.country) }
    var height by remember(profile) { mutableStateOf(profile.height.toString()) }
    var weight by remember(profile) { mutableStateOf(profile.weight.toString()) }
    var goalWeight by remember(profile) { mutableStateOf(profile.goalWeight.toString()) }
    
    // Measurements
    var waist by remember(profile) { mutableStateOf(profile.waist.toString()) }
    var chest by remember(profile) { mutableStateOf(profile.chest.toString()) }
    var hip by remember(profile) { mutableStateOf(profile.hip.toString()) }
    var neck by remember(profile) { mutableStateOf(profile.neck.toString()) }
    var bodyFat by remember(profile) { mutableStateOf(profile.bodyFat.toString()) }

    // Goal & Lifestyle
    var goal by remember(profile) { mutableStateOf(profile.goal) }
    var activityLevel by remember(profile) { mutableStateOf(profile.activityLevel) }
    var workoutExperience by remember(profile) { mutableStateOf(profile.workoutExperience) }
    var gymAvailability by remember(profile) { mutableStateOf(profile.gymAvailability) }

    // Multi-select Lists (Keto, PCOS, allergies, etc.)
    var selectedConditions by remember(profile) { mutableStateOf(profile.getMedicalConditionsList().toSet()) }
    var selectedFoodPref by remember(profile) { mutableStateOf(profile.foodPreferences) }
    var allergies by remember(profile) { mutableStateOf(profile.foodAllergies) }
    var equipment by remember(profile) { mutableStateOf(profile.equipmentAvailable) }

    // Show saved notification popup
    var showSavedMessage by remember { mutableStateOf(false) }

    // Recalculations on editing
    val parsedHeight = height.toDoubleOrNull() ?: 170.0
    val parsedWeight = weight.toDoubleOrNull() ?: 70.0
    val parsedAge = age.toIntOrNull() ?: 25
    
    val tempProfile = profile.copy(
        gender = gender,
        weight = parsedWeight,
        height = parsedHeight,
        age = parsedAge,
        activityLevel = activityLevel,
        goal = goal
    )

    val calculatedBmi = viewModel.calculateBmi(tempProfile)
    val bmr = viewModel.calculateBmr(tempProfile)
    val tdee = viewModel.calculateTdee(tempProfile)
    val calTarget = viewModel.calculateTargetCalories(tempProfile)
    val weightRange = viewModel.getHealthyWeightRange(tempProfile)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Health Analysis", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        modifier = Modifier.testTag("save_profile_btn"),
                        onClick = {
                            val updated = profile.copy(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                gender = gender,
                                age = age.toIntOrNull() ?: 25,
                                country = country,
                                height = height.toDoubleOrNull() ?: 170.0,
                                weight = weight.toDoubleOrNull() ?: 70.0,
                                goalWeight = goalWeight.toDoubleOrNull() ?: 65.0,
                                waist = waist.toDoubleOrNull() ?: 80.0,
                                chest = chest.toDoubleOrNull() ?: 95.0,
                                hip = hip.toDoubleOrNull() ?: 90.0,
                                neck = neck.toDoubleOrNull() ?: 38.0,
                                bodyFat = bodyFat.toDoubleOrNull() ?: 20.0,
                                goal = goal,
                                activityLevel = activityLevel,
                                workoutExperience = workoutExperience,
                                gymAvailability = gymAvailability,
                                medicalConditions = selectedConditions.joinToString(","),
                                foodPreferences = selectedFoodPref,
                                foodAllergies = allergies,
                                equipmentAvailable = equipment
                            )
                            viewModel.updateProfile(updated)
                            // Also log weight to track history
                            viewModel.logWeight(
                                weight = updated.weight,
                                bodyFat = updated.bodyFat,
                                waist = updated.waist,
                                chest = updated.chest,
                                hip = updated.hip,
                                neck = updated.neck
                            )
                            showSavedMessage = true
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Profile", tint = MaterialTheme.colorScheme.primary)
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
            // Saved Popup Note
            if (showSavedMessage) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSavedMessage = false }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, "Success", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Profile saved successfully! Scientific calculations updated.", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Real-time Scientific Calculations Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🧬 Live Calculations (Scientific Formula)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("BMI", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format("%.1f", calculatedBmi)} (${viewModel.getBmiCategory(calculatedBmi)})", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("BMR", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format("%.0f", bmr)} kcal", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("TDEE", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format("%.0f", tdee)} kcal", fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("DAILY CALORIE TARGET", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format("%.0f", calTarget)} kcal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("HEALTHY WEIGHT RANGE", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format("%.1f", weightRange.first)} - ${String.format("%.1f", weightRange.second)} kg", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section: Personal Details
            item {
                Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Country") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Gender: ", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = gender == "Male", onClick = { gender = "Male" })
                    Text("Male")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = gender == "Female", onClick = { gender = "Female" })
                    Text("Female")
                }
            }

            // Section: Body Dimensions
            item {
                Text("Body Dimensions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = goalWeight,
                        onValueChange = { goalWeight = it },
                        label = { Text("Goal Weight") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = waist,
                        onValueChange = { waist = it },
                        label = { Text("Waist (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = chest,
                        onValueChange = { chest = it },
                        label = { Text("Chest (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = hip,
                        onValueChange = { hip = it },
                        label = { Text("Hip (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = neck,
                        onValueChange = { neck = it },
                        label = { Text("Neck (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = bodyFat,
                    onValueChange = { bodyFat = it },
                    label = { Text("Estimated Body Fat % (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Section: Preferences, Experience and Goals
            item {
                Text("Goals & Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Text("Main Goal: $goal", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoalChip("Lose Weight", current = goal) { goal = it }
                    GoalChip("Gain Muscle", current = goal) { goal = it }
                    GoalChip("Fat Loss", current = goal) { goal = it }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoalChip("Maintain Weight", current = goal) { goal = it }
                    GoalChip("Body Recom", current = goal) { goal = "Body Recomposition" }
                }
            }

            item {
                Text("Activity Level: $activityLevel", fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Extremely Active").forEach { level ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activityLevel = level }
                        ) {
                            RadioButton(selected = activityLevel == level, onClick = { activityLevel = level })
                            Text(level, fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                Text("Preferences & Equipment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                OutlinedTextField(
                    value = selectedFoodPref,
                    onValueChange = { selectedFoodPref = it },
                    label = { Text("Food Preferences (Keto, Low Carb, Vegan, Halal, etc.)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Food Allergies / Intolerances") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = equipment,
                    onValueChange = { equipment = it },
                    label = { Text("Gym Equipment Available (Dumbbells, Bench, None...)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Section: Medical Conditions
            item {
                Text("Medical Conditions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Diabetes", "High Blood Pressure", "Heart Disease", "PCOS", "Pregnancy", "Joint Pain", "Thyroid").forEach { cond ->
                        val isChecked = selectedConditions.contains(cond)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedConditions = if (isChecked) {
                                        selectedConditions - cond
                                    } else {
                                        selectedConditions + cond
                                    }
                                }
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    selectedConditions = if (isChecked) {
                                        selectedConditions - cond
                                    } else {
                                        selectedConditions + cond
                                    }
                                }
                            )
                            Text(cond, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Bottom Space
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalChip(label: String, current: String, onSelect: (String) -> Unit) {
    FilterChip(
        selected = current == label,
        onClick = { onSelect(label) },
        label = { Text(label, fontSize = 12.sp) }
    )
}
