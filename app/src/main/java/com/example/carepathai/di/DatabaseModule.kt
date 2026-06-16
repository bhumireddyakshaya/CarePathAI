package com.example.carepathai.di

import android.content.Context
import androidx.room.Room
import com.example.carepathai.data.local.CarePathDatabase
import com.example.carepathai.data.local.dao.HealthHistoryDao
import com.example.carepathai.data.local.dao.MedicineDao
import com.example.carepathai.data.local.dao.WellnessDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CarePathDatabase {
        return Room.databaseBuilder(
            context,
            CarePathDatabase::class.java,
            "carepath_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideMedicineDao(database: CarePathDatabase): MedicineDao {
        return database.medicineDao()
    }

    @Provides
    fun provideHealthHistoryDao(database: CarePathDatabase): HealthHistoryDao {
        return database.healthHistoryDao()
    }

    @Provides
    fun provideWellnessDao(database: CarePathDatabase): WellnessDao {
        return database.wellnessDao()
    }
}
