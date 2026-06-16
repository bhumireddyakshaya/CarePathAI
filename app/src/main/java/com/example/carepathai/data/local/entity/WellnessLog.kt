package com.example.carepathai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wellness_logs")
data class WellnessLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long, // Start of the day timestamp
    val waterIntake: Int = 0, // in ml
    val sleepHours: Float = 0f,
    val steps: Int = 0,
    val caloriesBurned: Int = 0,
    val wellnessScore: Int = 0
)
