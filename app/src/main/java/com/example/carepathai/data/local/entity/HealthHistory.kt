package com.example.carepathai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_history")
data class HealthHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val symptoms: String, // Comma separated
    val diagnosis: String,
    val foodRecommendations: String,
    val exercisePlans: String,
    val riskLevel: String
)
