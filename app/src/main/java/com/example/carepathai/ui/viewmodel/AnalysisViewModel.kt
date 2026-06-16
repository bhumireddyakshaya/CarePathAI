package com.example.carepathai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carepathai.data.local.entity.HealthHistory
import com.example.carepathai.domain.repository.HealthHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Recommendation(val title: String, val content: String)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val historyRepository: HealthHistoryRepository
) : ViewModel() {

    private val _analysisResult = MutableStateFlow<HealthHistory?>(null)
    val analysisResult = _analysisResult.asStateFlow()

    fun performAnalysis(symptoms: List<String>) {
        viewModelScope.launch {
            // Mocking AI Analysis based on symptoms
            val diagnosis = if (symptoms.any { it.contains("fever", ignoreCase = true) }) "Fever"
            else if (symptoms.any { it.contains("sugar", ignoreCase = true) }) "Diabetes Risk"
            else if (symptoms.any { it.contains("chest", ignoreCase = true) }) "Heart Health"
            else "General Wellness"

            val foodRecs = getFoodRecommendations(diagnosis)
            val exerciseRecs = getExerciseRecommendations(diagnosis)
            
            val history = HealthHistory(
                date = System.currentTimeMillis(),
                symptoms = symptoms.joinToString(", "),
                diagnosis = diagnosis,
                foodRecommendations = foodRecs,
                exercisePlans = exerciseRecs,
                riskLevel = "Low"
            )
            
            _analysisResult.value = history
            historyRepository.insertHistory(history)
        }
    }

    private fun getFoodRecommendations(condition: String): String {
        return when (condition) {
            "Fever" -> "Fruits, Soup, Coconut Water, Vitamin C Rich Foods"
            "Diabetes Risk" -> "Oats, Whole Grains, Leafy Vegetables"
            "Heart Health" -> "Oats, Nuts, Fruits, Healthy Fats"
            else -> "Balanced Diet, Plenty of Water, Fresh Vegetables"
        }
    }

    private fun getExerciseRecommendations(condition: String): String {
        return when (condition) {
            "Fever" -> "Rest, Breathing Exercises"
            "Diabetes Risk" -> "Walking, Light Cardio"
            "Heart Health" -> "Walking, Yoga"
            else -> "Stretching, Light Cardio"
        }
    }
}
