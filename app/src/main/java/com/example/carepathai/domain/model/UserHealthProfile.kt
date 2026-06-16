package com.example.carepathai.domain.model

data class UserHealthProfile(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val mobileNumber: String = "",
    val age: Int = 0,
    val gender: String = "",
    val height: Float = 0f,
    val weight: Float = 0f,
    val bloodGroup: String = "",
    val medicalHistory: List<String> = emptyList(),
    val dietaryPreferences: String = "",
    val fitnessGoals: String = "",
    val profileImageUrl: String = ""
)
