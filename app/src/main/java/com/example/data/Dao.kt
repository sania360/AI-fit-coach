package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FitDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    // Weight Log
    @Query("SELECT * FROM weight_log ORDER BY date ASC")
    fun getAllWeightLogs(): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLog)

    @Query("DELETE FROM weight_log WHERE id = :id")
    suspend fun deleteWeightLog(id: Int)

    // Water Log
    @Query("SELECT * FROM water_log WHERE date = :date")
    fun getWaterLogsForDate(date: String): Flow<List<WaterLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLog)

    @Query("DELETE FROM water_log WHERE id = :id")
    suspend fun deleteWaterLog(id: Int)

    // Sleep Log
    @Query("SELECT * FROM sleep_log WHERE date = :date LIMIT 1")
    fun getSleepLogForDate(date: String): Flow<SleepLog?>

    @Query("SELECT * FROM sleep_log WHERE date = :date LIMIT 1")
    suspend fun getSleepLogForDateDirect(date: String): SleepLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(log: SleepLog)

    // Food Log
    @Query("SELECT * FROM food_log WHERE date = :date")
    fun getFoodLogsForDate(date: String): Flow<List<FoodLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(log: FoodLog)

    @Query("DELETE FROM food_log WHERE id = :id")
    suspend fun deleteFoodLog(id: Int)

    // Workout Log
    @Query("SELECT * FROM workout_log WHERE date = :date")
    fun getWorkoutLogsForDate(date: String): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_log ORDER BY date DESC")
    fun getAllWorkoutLogs(): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLog)

    @Query("DELETE FROM workout_log WHERE id = :id")
    suspend fun deleteWorkoutLog(id: Int)

    // Chat Message
    @Query("SELECT * FROM chat_message ORDER BY timestamp ASC")
    fun getChatHistory(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM chat_message")
    suspend fun clearChatHistory()
}
