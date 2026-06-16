package com.example.carepathai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.carepathai.data.local.dao.HealthHistoryDao
import com.example.carepathai.data.local.dao.MedicineDao
import com.example.carepathai.data.local.dao.WellnessDao
import com.example.carepathai.data.local.entity.HealthHistory
import com.example.carepathai.data.local.entity.Medicine
import com.example.carepathai.data.local.entity.WellnessLog

@Database(entities = [Medicine::class, HealthHistory::class, WellnessLog::class], version = 4, exportSchema = false)
abstract class CarePathDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun healthHistoryDao(): HealthHistoryDao
    abstract fun wellnessDao(): WellnessDao
}
