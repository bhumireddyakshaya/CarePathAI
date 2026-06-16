package com.example.carepathai.di

import com.example.carepathai.data.repository.AuthRepositoryImpl
import com.example.carepathai.data.repository.HealthHistoryRepositoryImpl
import com.example.carepathai.data.repository.HealthRepositoryImpl
import com.example.carepathai.data.repository.MedicineRepositoryImpl
import com.example.carepathai.domain.repository.AuthRepository
import com.example.carepathai.domain.repository.HealthHistoryRepository
import com.example.carepathai.domain.repository.HealthRepository
import com.example.carepathai.domain.repository.MedicineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMedicineRepository(
        medicineRepositoryImpl: MedicineRepositoryImpl
    ): MedicineRepository

    @Binds
    @Singleton
    abstract fun bindHealthHistoryRepository(
        healthHistoryRepositoryImpl: HealthHistoryRepositoryImpl
    ): HealthHistoryRepository

    @Binds
    @Singleton
    abstract fun bindHealthRepository(
        healthRepositoryImpl: HealthRepositoryImpl
    ): HealthRepository
}
