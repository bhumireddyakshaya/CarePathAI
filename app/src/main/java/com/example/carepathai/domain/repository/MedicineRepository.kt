package com.example.carepathai.domain.repository

import com.example.carepathai.data.local.entity.Medicine
import kotlinx.coroutines.flow.Flow

interface MedicineRepository {
    fun getAllMedicines(): Flow<List<Medicine>>
    suspend fun insertMedicine(medicine: Medicine)
    suspend fun updateMedicine(medicine: Medicine)
    suspend fun deleteMedicine(medicine: Medicine)
    suspend fun getMedicineById(id: Int): Medicine?
}
