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
                if (snapshot != null && snapshot.exists()) {
                    val profile = snapshot.toObject(UserHealthProfile::class.java)
                    if (profile != null) {
                        trySend(profile)
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateUserProfile(profile: UserHealthProfile) {
        // Assuming the profile object has the UID or it's managed externally
        // In a real scenario, we'd need the current user's UID here.
    }

    override suspend fun getAIAnalysis(symptoms: List<String>): String {
        return "AI Analysis for $symptoms"
    }
}
