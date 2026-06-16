package com.example.carepathai.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?
    fun login(email: String, password: String): Flow<Result<FirebaseUser>>
    fun signUp(email: String, password: String, name: String): Flow<Result<FirebaseUser>>
    fun logout()
    fun resetPassword(email: String): Flow<Result<Unit>>
}
