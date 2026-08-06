package com.example.carepathai.data.local.dao

import androidx.room.*
import com.example.carepathai.data.local.entity.HealthHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthHistoryDao {
    @Query("SELECT * FROM health_history ORDER BY date DESC")
    fun getAllHistory(): Flow<List<HealthHistory>>

    @Query("SELECT * FROM health_history WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getByFirestoreId(firestoreId: String): HealthHistory?

    @Query("SELECT * FROM health_history WHERE firestoreId = '' OR firestoreId IS NULL")
    suspend fun getUnsyncedHistory(): List<HealthHistory>

    @Query("DELETE FROM health_history WHERE firestoreId = :firestoreId")
    suspend fun deleteByFirestoreId(firestoreId: String)

    @Query("DELETE FROM health_history")
    suspend fun deleteAllHistory()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HealthHistory)

    @Delete
    suspend fun deleteHistory(history: HealthHistory)
}
