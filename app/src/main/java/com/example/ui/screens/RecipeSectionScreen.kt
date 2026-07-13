package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FitViewModel

data class Recipe(
    val name: String,
    val category: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val prepTime: String,
    val ingredients: List<String>,
    val instructions: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeSectionScreen(viewModel: FitViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var showAddedMessage by remember { mutableStateOf(false) }

    val recipes = listOf(
        Recipe(
            name = "Grilled Avocado & Chicken Salad",
            category = "Lunch/Dinner",
            calories = 420,
            protein = 35.0,
            carbs = 12.0,
            fat = 18.0,
            prepTime = "15 mins",
            ingredients = listOf("150g Grilled Chicken Breast", "1/2 Fresh Avocado", "2 cups Mixed Greens", "1 tbsp Olive Oil", "Lemon juice, Salt & Pepper"),
            instructions = "1. Grill the chicken breast until cooked.\n2. Slice the chicken and avocado.\n3. Toss mixed greens in olive oil and lemon juice.\n4. Top with sliced chicken and avocado, season to taste."
        ),
        Recipe(
            name = "High-Protein Almond Banana Oats",
            category = "Breakfast",
            calories = 380,
            protein = 16.0,
            carbs = 52.0,
            fat = 10.0,
            prepTime = "10 mins",
            ingredients = listOf("1/2 cup Rolled Oats", "1 cup Almond Milk", "1 scoop Whey Protein", "1/2 Sliced Banana", "10g Sliced Almonds"),
            instructions = "1. Cook oats in almond milk over medium heat.\n2. Remove from heat and stir in the protein powder.\n3. Top with sliced banana and almond slices."
        ),
        Recipe(
            name = "Baked Salmon with Broccoli & Quinoa",
            category = "Dinner",
            calories = 510,
            protein = 42.0,
            carbs = 38.0,
            fat = 16.0,
            prepTime = "25 mins",
            ingredients = listOf("150g Fresh Salmon Fillet", "1 cup Broccoli florets", "1/2 cup Cooked Quinoa", "1 tsp Garlic powder", "1 tbsp Soy sauce"),
            instructions = "1. Preheat oven to 400°F (200°C).\n2. Place salmon and broccoli on a baking sheet, drizzle with soy sauce and garlic powder.\n3. Bake for 15-20 minutes.\n4. Serve on top of cooked warm quinoa."
        ),
        Recipe(
            name = "Greek Yogurt Power Berry Bowl",
            category = "Breakfast/Snack",
            calories = 260,
            protein = 22.0,
            carbs = 28.0,
            fat = 4.0,
            prepTime = "5 mins",
            ingredients = listOf("1 cup Low-fat Greek Yogurt", "1/2 cup Fresh Blueberries & Strawberries", "1 tbsp Organic Honey", "1 tbsp Chia seeds"),
            instructions = "1. Spoon Greek yogurt into a clean bowl.\n2. Layer the fresh mixed berries on top.\n3. Sprinkle chia seeds and drizzle with honey."
        ),
        Recipe(
            name = "Mediterranean Chickpea & Hummus Wrap",
            category = "Snack/Lunch",
            calories = 340,
            protein = 12.0,
            carbs = 45.0,
            fat = 9.0,
            prepTime = "10 mins",
            ingredients = listOf("1 Whole Wheat Tortilla", "2 tbsp Creamy Hummus", "1/2 cup Rinse Chickpeas", "1/4 cup Diced Cucumber & Tomato", "Handful of Spinach"),
            instructions = "1. Spread hummus evenly across the wheat tortilla.\n2. Scatter chickpeas, cucumber, tomato, and spinach.\n3. Roll tightly, slice in half, and serve cold."
        )
    )

    val filteredRecipes = recipes.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedRecipe == null) "Healthy Recipes & Foods" else "Recipe Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (selectedRecipe != null) {
                        IconButton(onClick = { selectedRecipe = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (selectedRecipe == null) {
            // LIST VIEW WITH SEARCH
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search recipes or meals...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (showAddedMessage) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recipe added to your daily diary!", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text("Featured Fitness Recipes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRecipes) { recipe ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedRecipe = recipe },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(recipe.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("${recipe.category} • Prep: ${recipe.prepTime}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("P: ${recipe.protein.toInt()}g", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text("C: ${recipe.carbs.toInt()}g", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                        Text("F: ${recipe.fat.toInt()}g", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${recipe.calories} kcal", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                    IconButton(
                                        onClick = {
                                            viewModel.addFood(
                                                name = recipe.name,
                                                mealType = recipe.category.split("/").first(),
                                                calories = recipe.calories,
                                                protein = recipe.protein,
                                                carbs = recipe.carbs,
                                                fat = recipe.fat
                                            )
                                            showAddedMessage = true
                                        }
                                    ) {
                                        Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // DETAIL VIEW
            val rec = selectedRecipe!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(rec.name, fontWeight = FontWeight.Black, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Category: ${rec.category} • Cooking: ${rec.prepTime}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DetailMacroItem("Calories", "${rec.calories} kcal")
                            DetailMacroItem("Protein", "${rec.protein} g")
                            DetailMacroItem("Carbs", "${rec.carbs} g")
                            DetailMacroItem("Fat", "${rec.fat} g")
                        }
                    }
                }

                item {
                    Text("Ingredients", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                items(rec.ingredients) { ing ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(ing)
                    }
                }

                item {
                    Text("Step-by-Step Instructions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text(rec.instructions, modifier = Modifier.padding(16.dp), fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }

                item {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = {
                            viewModel.addFood(
                                name = rec.name,
                                mealType = rec.category.split("/").first(),
                                calories = rec.calories,
                                protein = rec.protein,
                                carbs = rec.carbs,
                                fat = rec.fat
                            )
                            selectedRecipe = null
                            showAddedMessage = true
                        }
                    ) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Today's Diary")
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun DetailMacroItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
