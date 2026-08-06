package com.example.carepathai.data.repository

import android.util.Log
import com.example.carepathai.data.local.dao.MedicineDao
import com.example.carepathai.data.local.entity.Medicine
import com.example.carepathai.domain.repository.MedicineRepository
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

class MedicineRepositoryImpl @Inject constructor(
    private val medicineDao: MedicineDao,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : MedicineRepository {

    override fun getAllMedicines(): Flow<List<Medicine>> = callbackFlow<List<Medicine>> {
        var firestoreListener: ListenerRegistration? = null
        var roomCollectorJob: kotlinx.coroutines.Job? = null

        fun attachListeners(userId: String?) {
            firestoreListener?.remove()
            firestoreListener = null
            roomCollectorJob?.cancel()
            roomCollectorJob = null

            if (userId == null) {
                roomCollectorJob = CoroutineScope(Dispatchers.IO).launch {
                    medicineDao.getAllMedicines().collect { list ->
                        trySend(list)
                    }
                }
            } else {
                roomCollectorJob = CoroutineScope(Dispatchers.IO).launch {
                    medicineDao.getAllMedicines().collect { roomList ->
                        trySend(roomList)
                    }
                }

                firestoreListener = firestore.collection("users")
                    .document(userId)
                    .collection("medicines")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("MedicineRepo", "Error listening to Firestore medicines", error)
                            return@addSnapshotListener
                        }
                        if (snapshot == null) return@addSnapshotListener

                        val remoteList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val name = doc.getString("name") ?: ""
                                val dosage = doc.getString("dosage") ?: ""
                                val frequency = doc.getString("frequency") ?: ""
                                val beforeFood = doc.getBoolean("beforeFood") ?: false
                                val doctorName = doc.getString("doctorName") ?: ""
                                val startDate = doc.getLong("startDate") ?: System.currentTimeMillis()
                                val endDate = doc.getLong("endDate") ?: (System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
                                val morning = doc.getBoolean("morning") ?: false
                                val afternoon = doc.getBoolean("afternoon") ?: false
                                val evening = doc.getBoolean("evening") ?: false
                                val night = doc.getBoolean("night") ?: false
                                val morningTime = doc.getString("morningTime")
                                val afternoonTime = doc.getString("afternoonTime")
                                val eveningTime = doc.getString("eveningTime")
                                val nightTime = doc.getString("nightTime")
                                val isTaken = doc.getBoolean("isTaken") ?: false
                                val lastTakenTimestamp = doc.getLong("lastTakenTimestamp")
                                val medicineImageUrl = doc.getString("medicineImageUrl") ?: ""
                                val notes = doc.getString("notes") ?: ""

                                Medicine(
                                    firestoreId = doc.id,
                                    name = name,
                                    dosage = dosage,
                                    frequency = frequency,
                                    beforeFood = beforeFood,
                                    doctorName = doctorName,
                                    startDate = startDate,
                                    endDate = endDate,
                                    morning = morning,
                                    afternoon = afternoon,
                                    evening = evening,
                                    night = night,
                                    morningTime = morningTime,
                                    afternoonTime = afternoonTime,
                                    eveningTime = eveningTime,
                                    nightTime = nightTime,
                                    isTaken = isTaken,
                                    lastTakenTimestamp = lastTakenTimestamp,
                                    medicineImageUrl = medicineImageUrl,
                                    notes = notes
                                )
                            } catch (e: Exception) {
                                Log.e("MedicineRepo", "Error parsing doc ${doc.id}", e)
                                null
                            }
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            remoteList.forEach { med ->
                                val existing = medicineDao.getByFirestoreId(med.firestoreId)
                                if (existing != null) {
                                    medicineDao.insertMedicine(med.copy(id = existing.id))
                                } else {
                                    medicineDao.insertMedicine(med)
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

    override suspend fun insertMedicine(medicine: Medicine) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val docRef = if (medicine.firestoreId.isNotEmpty()) {
                firestore.collection("users").document(user.uid)
                    .collection("medicines").document(medicine.firestoreId)
            } else {
                firestore.collection("users").document(user.uid)
                    .collection("medicines").document()
            }

            val updatedMed = if (medicine.firestoreId.isEmpty()) medicine.copy(firestoreId = docRef.id) else medicine
            val existing = medicineDao.getByFirestoreId(updatedMed.firestoreId)
            val medToInsert = if (existing != null) updatedMed.copy(id = existing.id) else updatedMed
            medicineDao.insertMedicine(medToInsert)

            val data = hashMapOf(
                "id" to docRef.id,
                "name" to updatedMed.name,
                "dosage" to updatedMed.dosage,
                "frequency" to updatedMed.frequency,
                "beforeFood" to updatedMed.beforeFood,
                "doctorName" to updatedMed.doctorName,
                "startDate" to updatedMed.startDate,
                "endDate" to updatedMed.endDate,
                "morning" to updatedMed.morning,
                "afternoon" to updatedMed.afternoon,
                "evening" to updatedMed.evening,
                "night" to updatedMed.night,
                "morningTime" to updatedMed.morningTime,
                "afternoonTime" to updatedMed.afternoonTime,
                "eveningTime" to updatedMed.eveningTime,
                "nightTime" to updatedMed.nightTime,
                "isTaken" to updatedMed.isTaken,
                "lastTakenTimestamp" to updatedMed.lastTakenTimestamp,
                "medicineImageUrl" to updatedMed.medicineImageUrl,
                "notes" to updatedMed.notes,
                "createdAt" to System.currentTimeMillis()
            )

            try {
                docRef.set(data).await()
            } catch (e: Exception) {
                Log.e("MedicineRepo", "Failed to insert medicine to Firestore", e)
            }
        } else {
            medicineDao.insertMedicine(medicine)
        }
    }

    override suspend fun updateMedicine(medicine: Medicine) {
        insertMedicine(medicine)
    }

    override suspend fun deleteMedicine(medicine: Medicine) {
        if (medicine.firestoreId.isNotEmpty()) {
            medicineDao.deleteByFirestoreId(medicine.firestoreId)
        }
        medicineDao.deleteMedicine(medicine)

        val user = firebaseAuth.currentUser
        if (user != null && medicine.firestoreId.isNotEmpty()) {
            try {
                firestore.collection("users").document(user.uid)
                    .collection("medicines").document(medicine.firestoreId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.e("MedicineRepo", "Failed to delete medicine from Firestore", e)
            }
        }
    }

    override suspend fun getMedicineById(id: Int): Medicine? = medicineDao.getMedicineById(id)
}
