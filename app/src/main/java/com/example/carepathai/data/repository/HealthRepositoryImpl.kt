package com.example.carepathai.data.repository

import com.example.carepathai.domain.model.UserHealthProfile
import com.example.carepathai.domain.repository.HealthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HealthRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : HealthRepository {
    
    override fun getUserProfile(userId: String): Flow<UserHealthProfile> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val profile = try {
                    if (snapshot != null && snapshot.exists()) {
                        val data = snapshot.data ?: emptyMap()
                        val fullName = (data["fullName"] ?: data["name"])?.toString() ?: ""
                        val email = data["email"]?.toString() ?: ""
                        val mobileNumber = (data["mobileNumber"] ?: data["mobile"])?.toString() ?: ""
                        val bloodGroup = (data["bloodGroup"] ?: data["blood"])?.toString() ?: ""
                        val gender = data["gender"]?.toString() ?: ""
                        val dietaryPreferences = data["dietaryPreferences"]?.toString() ?: ""
                        val fitnessGoals = data["fitnessGoals"]?.toString() ?: ""
                        val profileImageUrl = data["profileImageUrl"]?.toString() ?: ""
                        
                        val age = when (val a = data["age"]) {
                            is Number -> a.toInt()
                            is String -> a.toIntOrNull() ?: 0
                            else -> 0
                        }
                        val height = when (val h = data["height"]) {
                            is Number -> h.toFloat()
                            is String -> h.toFloatOrNull() ?: 0f
                            else -> 0f
                        }
                        val weight = when (val w = data["weight"]) {
                            is Number -> w.toFloat()
                            is String -> w.toFloatOrNull() ?: 0f
                            else -> 0f
                        }
                        @Suppress("UNCHECKED_CAST")
                        val medicalHistory = (data["medicalHistory"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

                        UserHealthProfile(
                            id = userId,
                            fullName = fullName,
                            email = email,
                            mobileNumber = mobileNumber,
                            age = age,
                            gender = gender,
                            height = height,
                            weight = weight,
                            bloodGroup = bloodGroup,
                            medicalHistory = medicalHistory,
                            dietaryPreferences = dietaryPreferences,
                            fitnessGoals = fitnessGoals,
                            profileImageUrl = profileImageUrl
                        )
                    } else {
                        UserHealthProfile(id = userId)
                    }
                } catch (e: Exception) {
                    UserHealthProfile(id = userId)
                }
                
                trySend(profile)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateUserProfile(profile: UserHealthProfile) {
        if (profile.id.isNotEmpty()) {
            val userData = mutableMapOf<String, Any>(
                "id" to profile.id,
                "fullName" to profile.fullName,
                "name" to profile.fullName,
                "email" to profile.email,
                "mobileNumber" to profile.mobileNumber,
                "mobile" to profile.mobileNumber,
                "age" to profile.age,
                "gender" to profile.gender,
                "height" to profile.height,
                "weight" to profile.weight,
                "bloodGroup" to profile.bloodGroup,
                "blood" to profile.bloodGroup,
                "medicalHistory" to profile.medicalHistory,
                "dietaryPreferences" to profile.dietaryPreferences,
                "fitnessGoals" to profile.fitnessGoals,
                "profileImageUrl" to profile.profileImageUrl
            )
            firestore.collection("users").document(profile.id)
                .set(userData, com.google.firebase.firestore.SetOptions.merge())
                .await()
        } else {
            throw Exception("User ID is missing. Cannot update profile.")
        }
    }

    override suspend fun getAIAnalysis(symptoms: List<String>): String {
        return "AI Analysis for $symptoms"
    }
}
