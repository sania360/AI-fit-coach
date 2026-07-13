package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.GeminiClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FitRepository

    // Current Date formatting
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentDateString: String get() = dateFormat.format(Date())

    // Flow states from DB
    val userProfile: StateFlow<UserProfile?>
    val weightLogs: StateFlow<List<WeightLog>>
    val chatHistory: StateFlow<List<ChatMessage>>

    // Date-specific reactive states (monitored by dashboard and trackers)
    private val _selectedDate = MutableStateFlow(currentDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val foodLogsForSelectedDate: StateFlow<List<FoodLog>>
    val waterLogsForSelectedDate: StateFlow<List<WaterLog>>
    val sleepLogForSelectedDate: StateFlow<SleepLog?>
    val workoutLogsForSelectedDate: StateFlow<List<WorkoutLog>>

    // AI Generation States
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisResult = MutableStateFlow("")
    val analysisResult: StateFlow<String> = _analysisResult.asStateFlow()

    private val _isGeneratingMeals = MutableStateFlow(false)
    val isGeneratingMeals: StateFlow<Boolean> = _isGeneratingMeals.asStateFlow()

    private val _mealPlanResult = MutableStateFlow("")
    val mealPlanResult: StateFlow<String> = _mealPlanResult.asStateFlow()

    private val _isGeneratingWorkouts = MutableStateFlow(false)
    val isGeneratingWorkouts: StateFlow<Boolean> = _isGeneratingWorkouts.asStateFlow()

    private val _workoutPlanResult = MutableStateFlow("")
    val workoutPlanResult: StateFlow<String> = _workoutPlanResult.asStateFlow()

    private val _isSendingChat = MutableStateFlow(false)
    val isSendingChat: StateFlow<Boolean> = _isSendingChat.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FitRepository(database.fitDao())

        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        weightLogs = repository.allWeightLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        chatHistory = repository.chatHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Combine selectedDate with date-specific streams
        foodLogsForSelectedDate = _selectedDate.flatMapLatest { date ->
            repository.getFoodLogsForDate(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        waterLogsForSelectedDate = _selectedDate.flatMapLatest { date ->
            repository.getWaterLogsForDate(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        sleepLogForSelectedDate = _selectedDate.flatMapLatest { date ->
            repository.getSleepLogForDate(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        workoutLogsForSelectedDate = _selectedDate.flatMapLatest { date ->
            repository.getWorkoutLogsForDate(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Create default profile if DB is completely empty on first launch
        viewModelScope.launch {
            val directProfile = repository.getUserProfileDirect()
            if (directProfile == null) {
                // Initialize clean default profile
                repository.insertUserProfile(
                    UserProfile(
                        id = 1,
                        firstName = "Fitness",
                        lastName = "Enthusiast",
                        email = "user@fitaicoach.com",
                        gender = "Male",
                        age = 28,
                        height = 178.0,
                        weight = 78.0,
                        goalWeight = 72.0,
                        waist = 88.0,
                        chest = 100.0,
                        hip = 95.0,
                        neck = 39.0,
                        bodyFat = 20.0,
                        goal = "Lose Weight",
                        activityLevel = "Moderately Active",
                        lastActiveDate = currentDateString
                    )
                )
                // Initialize default sample data for a robust dashboard on first run
                seedSampleData()
            }
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    // --- Local DB Mutations ---
    
    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.insertUserProfile(profile)
        }
    }

    fun logWeight(weight: Double, bodyFat: Double = 0.0, waist: Double = 0.0, chest: Double = 0.0, hip: Double = 0.0, neck: Double = 0.0) {
        viewModelScope.launch {
            repository.insertWeightLog(
                WeightLog(
                    date = currentDateString,
                    weight = weight,
                    bodyFat = bodyFat,
                    waist = waist,
                    chest = chest,
                    hip = hip,
                    neck = neck
                )
            )
            // Sync user profile weight as well
            val current = repository.getUserProfileDirect()
            if (current != null) {
                repository.insertUserProfile(current.copy(weight = weight, bodyFat = if (bodyFat > 0.0) bodyFat else current.bodyFat))
            }
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.insertWaterLog(WaterLog(date = currentDateString, amountMl = amountMl))
            addPoints(10) // Gamification points
        }
    }

    fun addSleep(hours: Double, quality: String, mood: String, energyLevel: String) {
        viewModelScope.launch {
            repository.insertSleepLog(
                SleepLog(
                    date = currentDateString,
                    hours = hours,
                    quality = quality,
                    mood = mood,
                    energyLevel = energyLevel
                )
            )
            addPoints(20)
        }
    }

    fun addFood(name: String, mealType: String, calories: Int, protein: Double, carbs: Double, fat: Double) {
        viewModelScope.launch {
            repository.insertFoodLog(
                FoodLog(
                    date = currentDateString,
                    mealType = mealType,
                    foodName = name,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat
                )
            )
            addPoints(15)
        }
    }

    fun deleteFood(id: Int) {
        viewModelScope.launch {
            repository.deleteFoodLog(id)
        }
    }

    fun addWorkout(name: String, durationMin: Int, calories: Int, type: String) {
        viewModelScope.launch {
            repository.insertWorkoutLog(
                WorkoutLog(
                    date = currentDateString,
                    workoutName = name,
                    durationMinutes = durationMin,
                    caloriesBurned = calories,
                    completed = true,
                    type = type
                )
            )
            // Increment Streaks
            val current = repository.getUserProfileDirect()
            if (current != null) {
                val newStreak = current.streakDays + 1
                repository.insertUserProfile(current.copy(streakDays = newStreak, points = current.points + 50))
            }
        }
    }

    fun deleteWorkout(id: Int) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(id)
        }
    }

    private fun addPoints(value: Int) {
        viewModelScope.launch {
            val current = repository.getUserProfileDirect() ?: return@launch
            val newPoints = current.points + value
            // Level up every 500 points
            val newLevel = (newPoints / 500) + 1
            repository.insertUserProfile(current.copy(points = newPoints, level = newLevel))
        }
    }

    // --- AI REST Queries via Coroutine Scope ---

    fun requestFitnessAnalysis() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            val profile = repository.getUserProfileDirect() ?: return@launch
            val result = GeminiClient.getFitnessAnalysis(profile)
            _analysisResult.value = result
            _isAnalyzing.value = false
        }
    }

    fun requestMealPlan() {
        viewModelScope.launch {
            _isGeneratingMeals.value = true
            val profile = repository.getUserProfileDirect() ?: return@launch
            val targetCalories = calculateTargetCalories(profile).toInt()
            val result = GeminiClient.generateMealPlan(profile, targetCalories)
            _mealPlanResult.value = result
            _isGeneratingMeals.value = false
        }
    }

    fun requestWorkoutPlan() {
        viewModelScope.launch {
            _isGeneratingWorkouts.value = true
            val profile = repository.getUserProfileDirect() ?: return@launch
            val result = GeminiClient.generateWorkoutPlan(profile)
            _workoutPlanResult.value = result
            _isGeneratingWorkouts.value = false
        }
    }

    fun sendChatMessage(text: String) {
        if (text.trim().isEmpty()) return
        viewModelScope.launch {
            // Save User message
            repository.insertChatMessage(ChatMessage(sender = "user", text = text))
            
            _isSendingChat.value = true
            val profile = repository.getUserProfileDirect() ?: return@launch
            val currentHistory = repository.chatHistory.first()
            
            // Query Gemini Coach with current profile & history context
            val aiReply = GeminiClient.chatWithCoach(profile, text, currentHistory)
            
            // Save AI message
            repository.insertChatMessage(ChatMessage(sender = "ai", text = aiReply))
            _isSendingChat.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
            // Add default welcome message from AI Coach
            val welcomeText = "Hi there! I'm Coach FitAI, your personal trainer and nutritionist. How can I help you crush your fitness goals today?"
            repository.insertChatMessage(ChatMessage(sender = "ai", text = welcomeText))
        }
    }

    // --- Scientific Calculators ---

    fun calculateBmi(profile: UserProfile): Double {
        val hM = profile.height / 100.0
        return if (hM > 0) profile.weight / (hM * hM) else 0.0
    }

    fun getBmiCategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Healthy Range"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    fun calculateBmr(profile: UserProfile): Double {
        return if (profile.gender == "Male") {
            88.362 + (13.397 * profile.weight) + (4.799 * profile.height) - (5.677 * profile.age)
        } else {
            447.593 + (9.247 * profile.weight) + (3.098 * profile.height) - (4.330 * profile.age)
        }
    }

    fun calculateTdee(profile: UserProfile): Double {
        val bmr = calculateBmr(profile)
        val activityFactor = when (profile.activityLevel) {
            "Sedentary" -> 1.2
            "Lightly Active" -> 1.375
            "Moderately Active" -> 1.55
            "Very Active" -> 1.725
            "Extremely Active" -> 1.9
            else -> 1.2
        }
        return bmr * activityFactor
    }

    fun calculateTargetCalories(profile: UserProfile): Double {
        val tdee = calculateTdee(profile)
        val adjustment = when (profile.goal) {
            "Lose Weight" -> -500.0
            "Fat Loss" -> -400.0
            "Gain Muscle" -> 300.0
            "Body Recomposition" -> -150.0
            "Athletic Performance" -> 100.0
            else -> 0.0
        }
        val minCal = if (profile.gender == "Male") 1500.0 else 1200.0
        return (tdee + adjustment).coerceAtLeast(minCal)
    }

    fun getHealthyWeightRange(profile: UserProfile): Pair<Double, Double> {
        val hM = profile.height / 100.0
        val min = 18.5 * (hM * hM)
        val max = 24.9 * (hM * hM)
        return Pair(min, max)
    }

    // Seeds default sample records so first time the user opens the app, the dashboard is beautiful and populated.
    private suspend fun seedSampleData() {
        val today = currentDateString
        
        // Clear old ones to prevent duplicates
        repository.insertWaterLog(WaterLog(date = today, amountMl = 250))
        repository.insertWaterLog(WaterLog(date = today, amountMl = 500))
        repository.insertWaterLog(WaterLog(date = today, amountMl = 250))

        repository.insertSleepLog(SleepLog(date = today, hours = 7.5, quality = "Restful", mood = "Focused", energyLevel = "High"))

        repository.insertFoodLog(FoodLog(date = today, mealType = "Breakfast", foodName = "Oatmeal with Almonds & Banana", calories = 380, protein = 12.0, carbs = 58.0, fat = 10.0))
        repository.insertFoodLog(FoodLog(date = today, mealType = "Lunch", foodName = "Grilled Chicken Salad", calories = 450, protein = 38.0, carbs = 15.0, fat = 14.0))
        repository.insertFoodLog(FoodLog(date = today, mealType = "Dinner", foodName = "Baked Salmon with Broccoli & Quinoa", calories = 520, protein = 40.0, carbs = 42.0, fat = 18.0))

        repository.insertWorkoutLog(WorkoutLog(date = today, workoutName = "Lower Body Hypertrophy", durationMinutes = 45, caloriesBurned = 320, completed = true, type = "Gym"))

        // Seed some history weight logs for progress chart
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (i in 5 downTo 1) {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -i * 3)
            val pastDate = sdf.format(cal.time)
            repository.insertWeightLog(
                WeightLog(
                    date = pastDate,
                    weight = 80.0 - (5 - i) * 0.4,
                    bodyFat = 21.0 - (5 - i) * 0.2,
                    waist = 89.0, chest = 101.0, hip = 96.0, neck = 39.0
                )
            )
        }

        // Add default chat history message
        val welcomeText = "Hi there! I'm Coach FitAI, your personal trainer and nutritionist. How can I help you crush your fitness goals today?"
        repository.insertChatMessage(ChatMessage(sender = "ai", text = welcomeText))
    }
}
