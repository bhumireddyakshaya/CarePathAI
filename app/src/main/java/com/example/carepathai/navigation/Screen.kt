package com.example.carepathai.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object SymptomAssessment : Screen("symptom_assessment")
    object AIAnalysis : Screen("ai_analysis")
    object FoodRecommendations : Screen("food_recommendations")
    object ExerciseRecommendations : Screen("exercise_recommendations")
    object MedicineReminder : Screen("medicine_reminder")
    object WellnessTracker : Screen("wellness_tracker")
    object NearbyDoctors : Screen("nearby_doctors")
    object History : Screen("history")
    object Profile : Screen("profile")
    object Emergency : Screen("emergency")
    object AIChatbot : Screen("ai_chatbot")
}
