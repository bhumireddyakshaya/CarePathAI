package com.example.carepathai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carepathai.data.local.entity.HealthHistory
import com.example.carepathai.data.repository.GeminiRepository
import com.example.carepathai.domain.repository.HealthHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Recommendation(val title: String, val content: String)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val historyRepository: HealthHistoryRepository,
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _analysisResult = MutableStateFlow<HealthHistory?>(null)
    val analysisResult = _analysisResult.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    fun performAnalysis(symptoms: List<String>) {
        viewModelScope.launch {
            // Attempt Gemini AI Analysis
            val geminiResult = geminiRepository.analyzeSymptomsWithGemini(symptoms)

            val diagnosis: String
            val foodRecs: String
            val exerciseRecs: String
            val finalRiskLevel: String

            if (geminiResult != null) {
                diagnosis = geminiResult.diagnosis
                foodRecs = geminiResult.foodRecommendations
                exerciseRecs = geminiResult.exercisePlans
                finalRiskLevel = geminiResult.riskLevel
            } else {
                // Local rule-based fallback
                val symptomsLower = symptoms.map { it.lowercase() }
                diagnosis = if (symptomsLower.any { it.contains("chest") || it.contains("heart") || it.contains("palpitations") }) "Cardiac Concern"
                else if (symptomsLower.any { it.contains("breath") || it.contains("wheezing") }) "Respiratory Issue"
                else if (symptomsLower.any { it.contains("sugar") || it.contains("thirst") }) "Metabolic Risk"
                else if (symptomsLower.any { it.contains("fever") || it.contains("chills") }) "Infection/Fever"
                else if (symptomsLower.any { it.contains("headache") || it.contains("migraine") }) "Neurological"
                else "General Wellness"

                foodRecs = getFoodRecommendations(diagnosis)
                exerciseRecs = getExerciseRecommendations(diagnosis)
                val baseRisk = when (diagnosis) {
                    "Cardiac Concern", "Respiratory Issue" -> "High"
                    "Metabolic Risk", "Infection/Fever" -> "Medium"
                    else -> "Low"
                }
                finalRiskLevel = if (symptoms.size > 4 && baseRisk == "Medium") "High" else baseRisk
            }
            
            val history = HealthHistory(
                date = System.currentTimeMillis(),
                symptoms = symptoms.joinToString(", "),
                diagnosis = diagnosis,
                foodRecommendations = foodRecs,
                exercisePlans = exerciseRecs,
                riskLevel = finalRiskLevel
            )
            
            _analysisResult.value = history
            try {
                historyRepository.insertHistory(history)
                val sourceLabel = if (geminiResult != null) "Gemini AI" else "CarePath Engine"
                _statusMessage.value = "Assessment ($sourceLabel) saved to Firebase successfully!"
            } catch (e: Exception) {
                e.printStackTrace()
                _statusMessage.value = "Firebase Save Error: ${e.localizedMessage ?: "Failed to save"}"
            }
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    private fun getFoodRecommendations(condition: String): String {
        return when (condition) {
            "Infection/Fever" -> "Fruits, Soup, Coconut Water, Vitamin C Rich Foods"
            "Metabolic Risk" -> "Oats, Whole Grains, Leafy Vegetables"
            "Cardiac Concern" -> "Oats, Nuts, Fruits, Healthy Fats"
            "Respiratory Issue" -> "Warm fluids, Honey, Ginger, Anti-inflammatory foods"
            else -> "Balanced Diet, Plenty of Water, Fresh Vegetables"
        }
    }

    private fun getExerciseRecommendations(condition: String): String {
        return when (condition) {
            "Infection/Fever" -> "Rest, Breathing Exercises"
            "Metabolic Risk" -> "Walking, Light Cardio"
            "Cardiac Concern" -> "Walking, Yoga (Consult Doctor First)"
            "Respiratory Issue" -> "Deep Breathing, Light Stretching"
            else -> "Stretching, Light Cardio"
        }
    }
}
