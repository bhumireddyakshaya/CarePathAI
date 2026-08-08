package com.example.carepathai.data.repository

import com.example.carepathai.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override fun login(email: String, password: String): Flow<Result<FirebaseUser>> = callbackFlow {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let {
                    trySend(Result.success(it))
                } ?: trySend(Result.failure(Exception("User is null")))
                close()
            }
            .addOnFailureListener {
                trySend(Result.failure(it))
                close()
            }
        awaitClose()
    }

    override fun signUp(email: String, password: String, name: String, mobileNumber: String): Flow<Result<FirebaseUser>> = callbackFlow {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    val userData = hashMapOf(
                        "id" to user.uid,
                        "fullName" to name,
                        "name" to name,
                        "email" to email,
                        "mobileNumber" to mobileNumber,
                        "mobile" to mobileNumber,
                        "age" to 0,
                        "bloodGroup" to "",
                        "blood" to "",
                        "gender" to "",
                        "height" to 0f,
                        "weight" to 0f,
                        "medicalHistory" to emptyList<String>(),
                        "dietaryPreferences" to "",
                        "fitnessGoals" to "",
                        "profileImageUrl" to "",
                        "createdAt" to System.currentTimeMillis()
                    )
                    firestore.collection("users").document(user.uid).set(userData)
                        .addOnSuccessListener {
                            trySend(Result.success(user))
                            close()
                        }
                        .addOnFailureListener { err ->
                            android.util.Log.e("AuthRepo", "Profile write warning on signup", err)
                            trySend(Result.success(user))
                            close()
                        }
                } else {
                    trySend(Result.failure(Exception("User is null")))
                    close()
                }
            }
            .addOnFailureListener {
                trySend(Result.failure(it))
                close()
            }
        awaitClose()
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun resetPassword(email: String): Flow<Result<Unit>> = callbackFlow {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                trySend(Result.success(Unit))
                close()
            }
            .addOnFailureListener {
                trySend(Result.failure(it))
                close()
            }
        awaitClose()
    }
}
