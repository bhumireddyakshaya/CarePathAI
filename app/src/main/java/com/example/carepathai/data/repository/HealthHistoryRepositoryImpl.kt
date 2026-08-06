package com.example.carepathai.data.repository

import android.util.Log
import com.example.carepathai.data.local.dao.HealthHistoryDao
import com.example.carepathai.data.local.entity.HealthHistory
import com.example.carepathai.domain.repository.HealthHistoryRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HealthHistoryRepositoryImpl @Inject constructor(
    private val healthHistoryDao: HealthHistoryDao,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : HealthHistoryRepository {

    override fun getAllHistory(): Flow<List<HealthHistory>> = callbackFlow<List<HealthHistory>> {
        var firestoreListener: ListenerRegistration? = null
        var roomCollectorJob: kotlinx.coroutines.Job? = null

        fun attachListeners(userId: String?) {
            firestoreListener?.remove()
            firestoreListener = null
            roomCollectorJob?.cancel()
            roomCollectorJob = null

            if (userId == null) {
                roomCollectorJob = CoroutineScope(Dispatchers.IO).launch {
                    healthHistoryDao.getAllHistory().collect { list ->
                        trySend(list)
                    }
                }
            } else {
                roomCollectorJob = CoroutineScope(Dispatchers.IO).launch {
                    healthHistoryDao.getAllHistory().collect { roomList ->
                        trySend(roomList)
                    }
                }

                firestoreListener = firestore.collection("users")
                    .document(userId)
                    .collection("health_history")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("HealthHistoryRepo", "Error listening to Firestore health_history", error)
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

                        CoroutineScope(Dispatchers.IO).launch {
                            val remoteFirestoreIds = remoteList.map { it.firestoreId }.toSet()
                            remoteList.forEach { history ->
                                val existing = healthHistoryDao.getByFirestoreId(history.firestoreId)
                                if (existing != null) {
                                    healthHistoryDao.insertHistory(history.copy(id = existing.id))
                                } else {
                                    healthHistoryDao.insertHistory(history)
                                }
                            }
                        }

                        trySend(remoteList)
                    }
            }
        }

        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            attachListeners(auth.currentUser?.uid)
        }

        firebaseAuth.addAuthStateListener(authStateListener)
        attachListeners(firebaseAuth.currentUser?.uid)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
            firestoreListener?.remove()
            roomCollectorJob?.cancel()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun insertHistory(history: HealthHistory) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val docRef = if (history.firestoreId.isNotEmpty()) {
                firestore.collection("users").document(user.uid)
                    .collection("health_history").document(history.firestoreId)
            } else {
                firestore.collection("users").document(user.uid)
                    .collection("health_history").document()
            }

            val updatedHistory = if (history.firestoreId.isEmpty()) history.copy(firestoreId = docRef.id) else history
            val existing = healthHistoryDao.getByFirestoreId(updatedHistory.firestoreId)
            val historyToInsert = if (existing != null) updatedHistory.copy(id = existing.id) else updatedHistory
            healthHistoryDao.insertHistory(historyToInsert)

            val data = hashMapOf(
                "id" to docRef.id,
                "date" to updatedHistory.date,
                "symptoms" to updatedHistory.symptoms,
                "diagnosis" to updatedHistory.diagnosis,
                "foodRecommendations" to updatedHistory.foodRecommendations,
                "exercisePlans" to updatedHistory.exercisePlans,
                "riskLevel" to updatedHistory.riskLevel,
                "createdAt" to System.currentTimeMillis()
            )

            try {
                docRef.set(data).await()
            } catch (e: Exception) {
                Log.e("HealthHistoryRepo", "Failed to insert history to Firestore", e)
            }
        } else {
            healthHistoryDao.insertHistory(history)
        }
    }

    override suspend fun deleteHistory(history: HealthHistory) {
        if (history.firestoreId.isNotEmpty()) {
            healthHistoryDao.deleteByFirestoreId(history.firestoreId)
        }
        healthHistoryDao.deleteHistory(history)

        val user = firebaseAuth.currentUser
        if (user != null && history.firestoreId.isNotEmpty()) {
            try {
                firestore.collection("users").document(user.uid)
                    .collection("health_history").document(history.firestoreId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.e("HealthHistoryRepo", "Failed to delete history from Firestore", e)
            }
        }
    }
}
