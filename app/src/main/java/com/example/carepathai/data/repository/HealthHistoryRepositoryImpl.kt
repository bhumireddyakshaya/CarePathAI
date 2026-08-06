package com.example.carepathai.data.repository

import android.util.Log
import com.example.carepathai.data.local.entity.HealthHistory
import com.example.carepathai.domain.repository.HealthHistoryRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HealthHistoryRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : HealthHistoryRepository {

    override fun getAllHistory(): Flow<List<HealthHistory>> = callbackFlow<List<HealthHistory>> {
        var firestoreListener: ListenerRegistration? = null

        fun attachListener(userId: String?) {
            firestoreListener?.remove()
            firestoreListener = null

            if (userId == null) {
                trySend(emptyList())
                return
            }

            firestoreListener = firestore.collection("users")
                .document(userId)
                .collection("health_history")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("HealthHistoryRepo", "Firestore snapshot error", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

                    val remoteList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val dateVal = doc.get("date")
                            val dateLong = when (dateVal) {
                                is Number -> dateVal.toLong()
                                is String -> dateVal.toLongOrNull() ?: System.currentTimeMillis()
                                is Timestamp -> dateVal.toDate().time
                                else -> System.currentTimeMillis()
                            }

                            val symptoms = doc.getString("symptoms") ?: ""
                            val diagnosis = doc.getString("diagnosis") ?: ""
                            val foodRecommendations = doc.getString("foodRecommendations") ?: doc.getString("food") ?: ""
                            val exercisePlans = doc.getString("exercisePlans") ?: doc.getString("exercise") ?: ""
                            val riskLevel = doc.getString("riskLevel") ?: ""

                            HealthHistory(
                                date = dateLong,
                                symptoms = symptoms,
                                diagnosis = diagnosis,
                                foodRecommendations = foodRecommendations,
                                exercisePlans = exercisePlans,
                                riskLevel = riskLevel,
                                firestoreId = doc.id
                            )
                        } catch (e: Exception) {
                            Log.e("HealthHistoryRepo", "Error parsing doc ${doc.id}", e)
                            null
                        }
                    }.sortedByDescending { it.date }

                    trySend(remoteList)
                }
        }

        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            attachListener(auth.currentUser?.uid)
        }

        firebaseAuth.addAuthStateListener(authStateListener)
        attachListener(firebaseAuth.currentUser?.uid)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
            firestoreListener?.remove()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun insertHistory(history: HealthHistory) {
        val user = firebaseAuth.currentUser ?: throw Exception("Not signed in to Firebase. Please log in first.")
        
        val docRef = if (history.firestoreId.isNotEmpty()) {
            firestore.collection("users").document(user.uid)
                .collection("health_history").document(history.firestoreId)
        } else {
            firestore.collection("users").document(user.uid)
                .collection("health_history").document()
        }

        val data = hashMapOf(
            "id" to docRef.id,
            "date" to history.date,
            "symptoms" to history.symptoms,
            "diagnosis" to history.diagnosis,
            "foodRecommendations" to history.foodRecommendations,
            "exercisePlans" to history.exercisePlans,
            "riskLevel" to history.riskLevel,
            "createdAt" to System.currentTimeMillis()
        )

        docRef.set(data).await()
    }

    override suspend fun deleteHistory(history: HealthHistory) {
        val user = firebaseAuth.currentUser ?: throw Exception("Not signed in to Firebase.")
        if (history.firestoreId.isNotEmpty()) {
            firestore.collection("users").document(user.uid)
                .collection("health_history").document(history.firestoreId)
                .delete()
                .await()
        }
    }
}
