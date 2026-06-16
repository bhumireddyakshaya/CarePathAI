package com.example.carepathai.domain.repository

import com.example.carepathai.domain.model.UserHealthProfile
import kotlinx.coroutines.flow.Flow

interface HealthRepository {
    fun getUserProfile(userId: String): Flow<UserHealthProfile>
    suspend fun updateUserProfile(profile: UserHealthProfile)
    suspend fun getAIAnalysis(symptoms: List<String>): String
}
