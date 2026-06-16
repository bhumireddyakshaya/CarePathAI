package com.example.carepathai.data.local.dao

import androidx.room.*
import com.example.carepathai.data.local.entity.HealthHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthHistoryDao {
    @Query("SELECT * FROM health_history ORDER BY date DESC")
    fun getAllHistory(): Flow<List<HealthHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HealthHistory)

    @Delete
    suspend fun deleteHistory(history: HealthHistory)
}
