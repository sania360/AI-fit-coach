package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val gender: String = "Male",
    val age: Int = 25,
    val country: String = "United States",
    val activityLevel: String = "Sedentary",
    val occupation: String = "",
    val height: Double = 175.0, // in cm
    val weight: Double = 70.0,  // in kg
    val goalWeight: Double = 68.0, // in kg
    val waist: Double = 80.0, // in cm
    val chest: Double = 95.0, // in cm
    val hip: Double = 90.0, // in cm
    val neck: Double = 38.0, // in cm
    val bodyFat: Double = 18.0, // percentage
    val medicalConditions: String = "", // Comma-separated list
    val foodPreferences: String = "None", // Comma-separated or single
    val foodAllergies: String = "", // Comma-separated list
    val workoutExperience: String = "Beginner",
    val gymAvailability: String = "Both", // Gym, Home, Both
    val equipmentAvailable: String = "None", // Comma-separated
    val sleepHours: Double = 8.0,
    val stressLevel: String = "Medium",
    val smoking: Boolean = false,
    val alcohol: String = "Never",
    val goal: String = "Lose Weight",
    val points: Int = 100,
    val level: Int = 1,
    val streakDays: Int = 1,
    val lastActiveDate: String = ""
) {
    // Medical helper
    fun getMedicalConditionsList(): List<String> {
        return if (medicalConditions.isEmpty()) emptyList() else medicalConditions.split(",").map { it.trim() }
    }

    // Equipment helper
    fun getEquipmentList(): List<String> {
        return if (equipmentAvailable.isEmpty()) emptyList() else equipmentAvailable.split(",").map { it.trim() }
    }
}

@Entity(tableName = "weight_log")
data class WeightLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val weight: Double,
    val bodyFat: Double,
    val waist: Double,
    val chest: Double,
    val hip: Double,
    val neck: Double
)

@Entity(tableName = "water_log")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val amountMl: Int
)

@Entity(tableName = "sleep_log")
data class SleepLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val hours: Double,
    val quality: String, // Restful, Normal, Disturbed
    val mood: String = "Good",
    val energyLevel: String = "Medium"
)

@Entity(tableName = "food_log")
data class FoodLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val mealType: String, // Breakfast, Snack, Lunch, Evening Snack, Dinner, Before Bed
    val foodName: String,
    val calories: Int,
    val protein: Double, // grams
    val carbs: Double,   // grams
    val fat: Double,     // grams
    val isRecipe: Boolean = false
)

@Entity(tableName = "workout_log")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val workoutName: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val completed: Boolean = true,
    val type: String = "Gym" // Gym, Home, Cardio, Custom
)

@Entity(tableName = "chat_message")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String, // user, ai
    val text: String
)
