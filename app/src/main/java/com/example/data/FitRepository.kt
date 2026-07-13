package com.example.data

import kotlinx.coroutines.flow.Flow

class FitRepository(private val fitDao: FitDao) {

    // User Profile
    val userProfile: Flow<UserProfile?> = fitDao.getUserProfile()
    suspend fun getUserProfileDirect(): UserProfile? = fitDao.getUserProfileDirect()
    suspend fun insertUserProfile(profile: UserProfile) = fitDao.insertUserProfile(profile)

    // Weight Log
    val allWeightLogs: Flow<List<WeightLog>> = fitDao.getAllWeightLogs()
    suspend fun insertWeightLog(log: WeightLog) = fitDao.insertWeightLog(log)
    suspend fun deleteWeightLog(id: Int) = fitDao.deleteWeightLog(id)

    // Water Log
    fun getWaterLogsForDate(date: String): Flow<List<WaterLog>> = fitDao.getWaterLogsForDate(date)
    suspend fun insertWaterLog(log: WaterLog) = fitDao.insertWaterLog(log)
    suspend fun deleteWaterLog(id: Int) = fitDao.deleteWaterLog(id)

    // Sleep Log
    fun getSleepLogForDate(date: String): Flow<SleepLog?> = fitDao.getSleepLogForDate(date)
    suspend fun getSleepLogForDateDirect(date: String): SleepLog? = fitDao.getSleepLogForDateDirect(date)
    suspend fun insertSleepLog(log: SleepLog) = fitDao.insertSleepLog(log)

    // Food Log
    fun getFoodLogsForDate(date: String): Flow<List<FoodLog>> = fitDao.getFoodLogsForDate(date)
    suspend fun insertFoodLog(log: FoodLog) = fitDao.insertFoodLog(log)
    suspend fun deleteFoodLog(id: Int) = fitDao.deleteFoodLog(id)

    // Workout Log
    fun getWorkoutLogsForDate(date: String): Flow<List<WorkoutLog>> = fitDao.getWorkoutLogsForDate(date)
    val allWorkoutLogs: Flow<List<WorkoutLog>> = fitDao.getAllWorkoutLogs()
    suspend fun insertWorkoutLog(log: WorkoutLog) = fitDao.insertWorkoutLog(log)
    suspend fun deleteWorkoutLog(id: Int) = fitDao.deleteWorkoutLog(id)

    // Chat Message
    val chatHistory: Flow<List<ChatMessage>> = fitDao.getChatHistory()
    suspend fun insertChatMessage(message: ChatMessage) = fitDao.insertChatMessage(message)
    suspend fun clearChatHistory() = fitDao.clearChatHistory()
}
