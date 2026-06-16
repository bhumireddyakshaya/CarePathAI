package com.example.carepathai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String,
    val frequency: String,
    val beforeFood: Boolean = false,
    val doctorName: String = "",
    val startDate: Long,
    val endDate: Long,
    val morning: Boolean = false,
    val afternoon: Boolean = false,
    val evening: Boolean = false,
    val night: Boolean = false,
    val morningTime: String? = null,
    val afternoonTime: String? = null,
    val eveningTime: String? = null,
    val nightTime: String? = null,
    val isTaken: Boolean = false,
    val lastTakenTimestamp: Long? = null,
    val medicineImageUrl: String = "",
    val notes: String = ""
)
