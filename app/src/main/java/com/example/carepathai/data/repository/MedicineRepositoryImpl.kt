package com.example.carepathai.data.repository

import android.util.Log
import com.example.carepathai.data.local.entity.Medicine
import com.example.carepathai.domain.repository.MedicineRepository
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

class MedicineRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : MedicineRepository {

    override fun getAllMedicines(): Flow<List<Medicine>> = callbackFlow<List<Medicine>> {
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
                .collection("medicines")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("MedicineRepo", "Firestore error", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

                    val remoteList = snapshot.documents.mapNotNull { doc ->
                        try {
                            Medicine(
                                firestoreId = doc.id,
                                name = doc.getString("name") ?: "",
                                dosage = doc.getString("dosage") ?: "",
                                frequency = doc.getString("frequency") ?: "",
                                beforeFood = doc.getBoolean("beforeFood") ?: false,
                                doctorName = doc.getString("doctorName") ?: "",
                                startDate = doc.getLong("startDate") ?: System.currentTimeMillis(),
                                endDate = doc.getLong("endDate") ?: (System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000),
                                morning = doc.getBoolean("morning") ?: false,
                                afternoon = doc.getBoolean("afternoon") ?: false,
                                evening = doc.getBoolean("evening") ?: false,
                                night = doc.getBoolean("night") ?: false,
                                morningTime = doc.getString("morningTime"),
                                afternoonTime = doc.getString("afternoonTime"),
                                eveningTime = doc.getString("eveningTime"),
                                nightTime = doc.getString("nightTime"),
                                isTaken = doc.getBoolean("isTaken") ?: false,
                                lastTakenTimestamp = doc.getLong("lastTakenTimestamp"),
                                medicineImageUrl = doc.getString("medicineImageUrl") ?: "",
                                notes = doc.getString("notes") ?: ""
                            )
                        } catch (e: Exception) {
                            Log.e("MedicineRepo", "Error parsing doc ${doc.id}", e)
                            null
                        }
                    }

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

    override suspend fun insertMedicine(medicine: Medicine) {
        val user = firebaseAuth.currentUser ?: throw Exception("Not signed in to Firebase. Please log in first.")
        val docRef = if (medicine.firestoreId.isNotEmpty()) {
            firestore.collection("users").document(user.uid)
                .collection("medicines").document(medicine.firestoreId)
        } else {
            firestore.collection("users").document(user.uid)
                .collection("medicines").document()
        }

        val data = hashMapOf(
            "id" to docRef.id,
            "name" to medicine.name,
            "dosage" to medicine.dosage,
            "frequency" to medicine.frequency,
            "beforeFood" to medicine.beforeFood,
            "doctorName" to medicine.doctorName,
            "startDate" to medicine.startDate,
            "endDate" to medicine.endDate,
            "morning" to medicine.morning,
            "afternoon" to medicine.afternoon,
            "evening" to medicine.evening,
            "night" to medicine.night,
            "morningTime" to medicine.morningTime,
            "afternoonTime" to medicine.afternoonTime,
            "eveningTime" to medicine.eveningTime,
            "nightTime" to medicine.nightTime,
            "isTaken" to medicine.isTaken,
            "lastTakenTimestamp" to medicine.lastTakenTimestamp,
            "medicineImageUrl" to medicine.medicineImageUrl,
            "notes" to medicine.notes,
            "createdAt" to System.currentTimeMillis()
        )

        docRef.set(data).await()
    }

    override suspend fun updateMedicine(medicine: Medicine) {
        insertMedicine(medicine)
    }

    override suspend fun deleteMedicine(medicine: Medicine) {
        val user = firebaseAuth.currentUser ?: throw Exception("Not signed in to Firebase. Please log in first.")
        if (medicine.firestoreId.isNotEmpty()) {
            firestore.collection("users").document(user.uid)
                .collection("medicines").document(medicine.firestoreId)
                .delete()
                .await()
        }
    }

    override suspend fun getMedicineById(id: Int): Medicine? = null
}
