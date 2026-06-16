package com.example.carepathai.data.repository

import com.example.carepathai.data.local.dao.MedicineDao
import com.example.carepathai.data.local.entity.Medicine
import com.example.carepathai.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MedicineRepositoryImpl @Inject constructor(
    private val medicineDao: MedicineDao
) : MedicineRepository {
    override fun getAllMedicines(): Flow<List<Medicine>> = medicineDao.getAllMedicines()
    override suspend fun insertMedicine(medicine: Medicine) = medicineDao.insertMedicine(medicine)
    override suspend fun updateMedicine(medicine: Medicine) = medicineDao.updateMedicine(medicine)
    override suspend fun deleteMedicine(medicine: Medicine) = medicineDao.deleteMedicine(medicine)
    override suspend fun getMedicineById(id: Int): Medicine? = medicineDao.getMedicineById(id)
}
