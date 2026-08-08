package com.example.carepathai.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class GeminiAnalysisResult(
    val diagnosis: String,
    val riskLevel: String,
    val foodRecommendations: String,
    val exercisePlans: String
)

@Singleton
class GeminiRepository @Inject constructor() {

    private val apiKey = "AIzaSyD4vmPx2VshhFUdnMLBpxEYu_e8YDwIIYk"
    private val endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

    suspend fun analyzeSymptomsWithGemini(symptoms: List<String>): GeminiAnalysisResult? = withContext(Dispatchers.IO) {
        try {
            val symptomsText = symptoms.joinToString(", ")
            val prompt = """
                You are a clinical nutrition and symptom analysis AI assistant for CarePathAI.
                Analyze the following patient symptoms: "$symptomsText".
                Provide a response in strict JSON format with exactly these key names:
                {
                  "diagnosis": "A concise primary diagnosis or concern",
                  "riskLevel": "Low", "Medium", or "High",
                  "foodRecommendations": "Specific comma-separated list of recommended foods, nutrients, and hydration advice",
                  "exercisePlans": "Recommended light or tailored exercise and recovery activities"
                }
                Do not include markdown code block syntax or extra text outside JSON.
            """.trimIndent()

            val url = URL(endpointUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            val requestBody = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val textObj = JSONObject()

            textObj.put("text", prompt)
            partsArray.put(textObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBody.put("contents", contentsArray)

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(requestBody.toString())
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseText)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        var rawText = parts.getJSONObject(0).optString("text", "").trim()
                        if (rawText.startsWith("```json")) {
                            rawText = rawText.removePrefix("```json").removeSuffix("```").trim()
                        } else if (rawText.startsWith("```")) {
                            rawText = rawText.removePrefix("```").removeSuffix("```").trim()
                        }

                        val parsedJson = JSONObject(rawText)
                        val diagnosis = parsedJson.optString("diagnosis", "General Assessment")
                        val riskLevel = parsedJson.optString("riskLevel", "Low")
                        val foodRecs = parsedJson.optString("foodRecommendations", "Balanced Diet, Water, Fresh Vegetables")
                        val exerciseRecs = parsedJson.optString("exercisePlans", "Light Walking, Rest")

                        return@withContext GeminiAnalysisResult(
                            diagnosis = diagnosis,
                            riskLevel = riskLevel,
                            foodRecommendations = foodRecs,
                            exercisePlans = exerciseRecs
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
