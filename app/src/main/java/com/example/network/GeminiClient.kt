package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.data.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Call Gemini API to generate content with a clean system prompt and user input
     */
    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not set or using placeholder.")
            return@withContext "Error: Gemini API key is not configured. Please add your GEMINI_API_KEY in the AI Studio Secrets panel."
        }

        try {
            val requestBodyJson = JSONObject()
            
            // Contents list
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            contentObj.put("role", "user")
            
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBodyJson.put("contents", contentsArray)

            // System instructions
            if (systemInstruction != null) {
                val systemInstructionObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                systemInstructionObj.put("parts", sysPartsArray)
                requestBodyJson.put("systemInstruction", systemInstructionObj)
            }

            // Generation config
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.7)
            requestBodyJson.put("generationConfig", generationConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "API Call failed: ${response.code} - $errBody")
                    return@withContext "Error: Failed to call AI coach (${response.code})."
                }

                val responseBodyStr = response.body?.string()
                if (responseBodyStr.isNullOrEmpty()) {
                    return@withContext "Error: Received empty response from AI coach."
                }

                val jsonResponse = JSONObject(responseBodyStr)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No text content generated.")
                        }
                    }
                }
                return@withContext "Error: Could not parse AI response contents."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini Call", e)
            return@withContext "Error: Network connection failed. Please check your internet connection. Details: ${e.localizedMessage}"
        }
    }

    /**
     * Generates a comprehensive fitness analysis using user profile and scientific formulas
     */
    suspend fun getFitnessAnalysis(profile: UserProfile): String {
        // Calculate basic metabolic details scientifically
        val heightM = profile.height / 100.0
        val bmi = profile.weight / (heightM * heightM)
        
        // BMR via Harris-Benedict Equation
        val bmr = if (profile.gender == "Male") {
            88.362 + (13.397 * profile.weight) + (4.799 * profile.height) - (5.677 * profile.age)
        } else {
            447.593 + (9.247 * profile.weight) + (3.098 * profile.height) - (4.330 * profile.age)
        }

        // TDEE Factor
        val activityFactor = when (profile.activityLevel) {
            "Sedentary" -> 1.2
            "Lightly Active" -> 1.375
            "Moderately Active" -> 1.55
            "Very Active" -> 1.725
            "Extremely Active" -> 1.9
            else -> 1.2
        }
        val tdee = bmr * activityFactor

        val goalCalorieAdjustment = when (profile.goal) {
            "Lose Weight" -> -500.0
            "Fat Loss" -> -400.0
            "Gain Muscle" -> 300.0
            "Body Recomposition" -> -150.0
            "Athletic Performance" -> 100.0
            else -> 0.0
        }
        val targetCalories = (tdee + goalCalorieAdjustment).coerceAtLeast(1200.0)

        val systemPrompt = "You are an elite Sports Nutritionist, Certified Fitness Trainer, and Medical Advisor. You speak in a highly motivating, supportive, clear, and professional tone."
        
        val prompt = """
            Please analyze this user's fitness profile and provide a scientific breakdown with motivational advice.
            
            Scientific Calculations Provided:
            - BMI: ${String.format("%.2f", bmi)}
            - BMR (Basal Metabolic Rate): ${String.format("%.0f", bmr)} kcal
            - TDEE (Total Daily Energy Expenditure): ${String.format("%.0f", tdee)} kcal
            - Recommended Daily Calorie Intake for goal (${profile.goal}): ${String.format("%.0f", targetCalories)} kcal
            
            User Profile:
            - Name: ${profile.firstName} ${profile.lastName}
            - Gender: ${profile.gender}
            - Age: ${profile.age} years old
            - Height: ${profile.height} cm
            - Weight: ${profile.weight} kg
            - Goal Weight: ${profile.goalWeight} kg
            - Body Measurements: Waist ${profile.waist} cm, Chest ${profile.chest} cm, Hip ${profile.hip} cm, Neck ${profile.neck} cm
            - Estimated Body Fat: ${profile.bodyFat}%
            - Activity Level: ${profile.activityLevel}
            - Medical Conditions: ${if (profile.medicalConditions.isEmpty()) "None declared" else profile.medicalConditions}
            - Food Preferences: ${profile.foodPreferences}
            - Food Allergies: ${if (profile.foodAllergies.isEmpty()) "None declared" else profile.foodAllergies}
            - Experience Level: ${profile.workoutExperience}
            - Workout Location: ${profile.gymAvailability}
            
            Please structure your response into the following clear sections:
            1. **Current Fitness Status & BMI Category**: Briefly explain their BMI category and body measurements in a supportive way.
            2. **Lifestyle & Risk Factors**: Analyze their current lifestyle choices, stress levels, and sleep.
            3. **Medical Considerations & Warnings**: If they have medical conditions (e.g., High Blood Pressure, PCOS, Joint Pain) or food allergies, give actionable, highly professional medical/safe warnings and precautions.
            4. **Body Analysis & Calorie Explanation**: Explain BMR, TDEE, and why the calorie target of ${String.format("%.0f", targetCalories)} kcal is ideal for their goal of '${profile.goal}'.
            
            Keep formatting clean with bullet points and bold headers so it is beautiful and easily readable on an Android phone screen. Do not use unnecessary markdown formatting.
        """.trimIndent()

        return generateContent(prompt, systemPrompt)
    }

    /**
     * Generates a 7-day personalized meal plan based on the user's profile and calorie calculations
     */
    suspend fun generateMealPlan(profile: UserProfile, targetCalories: Int): String {
        val systemPrompt = "You are an expert Personal Chef, Certified Sports Dietitian, and AI Fitness Trainer. You generate scientifically backed nutrition and diet plans."
        
        val prompt = """
            Create a comprehensive, personalized 7-Day Meal Plan based on this user's fitness goals and profile.
            
            User Profile:
            - Goal: ${profile.goal}
            - Daily Calories Target: $targetCalories kcal
            - Gender: ${profile.gender}, Age: ${profile.age}, Height: ${profile.height} cm, Weight: ${profile.weight} kg
            - Food Preferences: ${profile.foodPreferences}
            - Food Allergies: ${if (profile.foodAllergies.isEmpty()) "None" else profile.foodAllergies}
            
            Requirements:
            Provide a clear and complete nutrition guide containing:
            1. **Macro Splits Target**: State target Protein, Carbs, and Fat in grams.
            2. **7-Day Meal Table**: For Monday to Sunday, list meals for:
               - Breakfast
               - Mid-day Snack
               - Lunch
               - Evening Snack
               - Dinner
               - Before Bed (Optional/Light)
               List simple, real-food options matching their dietary preference (${profile.foodPreferences}).
            3. **Sample Recipe & Cooking Instructions**: Provide step-by-step instructions for one main recipe from the list.
            4. **Water Tracker Target & Meal Timing Guide**: Define their water target in ml/cups and when to eat these meals.
            5. **Smart Grocery List**: List essential items to buy for this week.
            
            Format nicely using bullet points, subheadings, and bold text for visual structure. Keep the advice practical and delicious.
        """.trimIndent()

        return generateContent(prompt, systemPrompt)
    }

    /**
     * Generates a personalized weekly workout plan
     */
    suspend fun generateWorkoutPlan(profile: UserProfile): String {
        val systemPrompt = "You are a world-class Strength & Conditioning Coach, Kinesiologist, and Certified Fitness Instructor."
        
        val prompt = """
            Create a highly personalized 7-Day Workout Plan tailored specifically to this user's profile.
            
            User Profile:
            - Main Goal: ${profile.goal}
            - Workout Experience: ${profile.workoutExperience}
            - Workout Location: ${profile.gymAvailability} (Home, Gym, or Both)
            - Equipment Available: ${if (profile.equipmentAvailable.isEmpty()) "None (Bodyweight)" else profile.equipmentAvailable}
            - Medical Conditions / Joint Pain: ${if (profile.medicalConditions.isEmpty()) "None declared" else profile.medicalConditions}
            
            Requirements:
            Provide a complete structured weekly workout routine:
            1. **Weekly Split Strategy**: E.g., Upper/Lower, Push/Pull/Legs, or Full Body depending on experience (${profile.workoutExperience}) and goals.
            2. **7-Day Daily Schedule** (Monday to Sunday):
               - For active days, list the routine including:
                 - Warm Up (duration & dynamic exercises)
                 - Main Exercises (name, muscle group, sets, reps, rest time, tempo)
                 - Cooldown (static stretching, deep breathing)
                 - Cardio / HIIT / LISS recommendations
               - For recovery days, designate active recovery (walking, mobility, stretching) or full rest.
            3. **Common Mistakes & Safety Tips**: Focus specifically on safety guidelines given their conditions (${profile.medicalConditions}) and experience.
            4. **Alternative Exercises**: List 3 alternative exercises if standard ones cannot be performed.
            
            Please use bullet points, bold terms, and a beautifully structured markdown format that displays perfectly on a mobile viewport.
        """.trimIndent()

        return generateContent(prompt, systemPrompt)
    }

    /**
     * Custom Chatbot Coach response
     */
    suspend fun chatWithCoach(profile: UserProfile, message: String, chatHistory: List<com.example.data.ChatMessage>): String {
        val systemPrompt = """
            You are "Coach FitAI", an advanced personal AI fitness coach, nutritionist, and mental motivator. 
            You know everything about the user and will tailor your answers specifically to their details:
            - User Name: ${profile.firstName} ${profile.lastName}
            - Gender: ${profile.gender}
            - Age: ${profile.age}
            - Current Weight: ${profile.weight} kg, Goal Weight: ${profile.goalWeight} kg, Height: ${profile.height} cm
            - Fitness Goal: ${profile.goal}
            - Medical Conditions: ${if (profile.medicalConditions.isEmpty()) "None" else profile.medicalConditions}
            - Diet preference: ${profile.foodPreferences}
            - Allergies: ${if (profile.foodAllergies.isEmpty()) "None" else profile.foodAllergies}
            - Gym Access: ${profile.gymAvailability}, Equipment: ${profile.equipmentAvailable}
            - Experience: ${profile.workoutExperience}
            
            Guidelines:
            - Be highly personalized. Address them by name occasionally.
            - Answer scientific fitness, diet, or weight questions with exact details, but in simple language.
            - Give direct, actionable advice. (e.g., if asked "Can I eat pizza?", explain how it fits into their calorie budget and macros for ${profile.goal}, rather than just saying a flat yes or no).
            - Highlight any safety precautions if relevant to their medical conditions (e.g. if they have joint pain or high blood pressure).
            - Keep responses conversational, concise, and easy to read on mobile. Avoid massive walls of text. Use bullet points for structured tips.
        """.trimIndent()

        val historyPrompt = StringBuilder()
        chatHistory.takeLast(10).forEach { msg ->
            val role = if (msg.sender == "user") "User" else "Coach"
            historyPrompt.append("$role: ${msg.text}\n")
        }
        historyPrompt.append("User: $message\nCoach:")

        return generateContent(historyPrompt.toString(), systemPrompt)
    }
}
