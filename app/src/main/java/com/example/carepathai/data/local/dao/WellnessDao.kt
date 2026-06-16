package com.example.carepathai.data.local.dao

import androidx.room.*
import com.example.carepathai.data.local.entity.WellnessLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessDao {
    @Query("SELECT * FROM wellness_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<WellnessLog>>

    @Query("SELECT * FROM wellness_logs WHERE date = :date LIMIT 1")
    suspend fun getLogByDate(date: Long): WellnessLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WellnessLog)

    @Update
    suspend fun updateLog(log: WellnessLog)
}
