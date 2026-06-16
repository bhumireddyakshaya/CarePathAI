package com.example.carepathai.domain.repository

import com.example.carepathai.data.local.entity.HealthHistory
import kotlinx.coroutines.flow.Flow

interface HealthHistoryRepository {
    fun getAllHistory(): Flow<List<HealthHistory>>
    suspend fun insertHistory(history: HealthHistory)
    suspend fun deleteHistory(history: HealthHistory)
}
