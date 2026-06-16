package com.example.carepathai.data.repository

import com.example.carepathai.data.local.dao.HealthHistoryDao
import com.example.carepathai.data.local.entity.HealthHistory
import com.example.carepathai.domain.repository.HealthHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HealthHistoryRepositoryImpl @Inject constructor(
    private val healthHistoryDao: HealthHistoryDao
) : HealthHistoryRepository {
    override fun getAllHistory(): Flow<List<HealthHistory>> = healthHistoryDao.getAllHistory()
    override suspend fun insertHistory(history: HealthHistory) = healthHistoryDao.insertHistory(history)
    override suspend fun deleteHistory(history: HealthHistory) = healthHistoryDao.deleteHistory(history)
}
