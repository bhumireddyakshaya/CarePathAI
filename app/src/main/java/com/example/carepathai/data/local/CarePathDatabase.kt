package com.example.carepathai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.carepathai.data.local.dao.HealthHistoryDao
import com.example.carepathai.data.local.dao.MedicineDao
import com.example.carepathai.data.local.entity.HealthHistory
import com.example.carepathai.data.local.entity.Medicine

@Database(entities = [Medicine::class, HealthHistory::class], version = 6, exportSchema = false)
abstract class CarePathDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun healthHistoryDao(): HealthHistoryDao
}
