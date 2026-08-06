package com.example.carepathai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carepathai.domain.model.UserHealthProfile
import com.example.carepathai.domain.repository.AuthRepository
import com.example.carepathai.domain.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: HealthRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserHealthProfile())
    val userProfile = _userProfile.asStateFlow()

    private val _updateStatus = MutableStateFlow<String?>(null)
    val updateStatus = _updateStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        authRepository.currentUser?.uid?.let { uid ->
            _userProfile.value = _userProfile.value.copy(id = uid)
            loadUserProfile()
        }
    }

    private fun loadUserProfile() {
        authRepository.currentUser?.let { user ->
            val uid = user.uid
            val email = user.email ?: ""
            viewModelScope.launch {
                _isLoading.value = true
                repository.getUserProfile(uid)
                    .catch { e -> 
                        e.printStackTrace()
                        _updateStatus.value = "Error loading profile: ${e.localizedMessage}"
                        _isLoading.value = false
                    }
                    .collect { profile ->
                        if (profile.fullName.isEmpty() && profile.email.isEmpty()) {
                            // Document likely missing, auto-create it with basic auth info
                            val initialProfile = UserHealthProfile(
                                id = uid,
                                email = email,
                                fullName = "User"
                            )
                            repository.updateUserProfile(initialProfile)
                            _userProfile.value = initialProfile
                        } else {
                            _userProfile.value = profile.copy(id = uid)
                        }
                        _isLoading.value = false
                    }
            }
        }
    }

    fun updateProfile(profile: UserHealthProfile) {
        val uid = authRepository.currentUser?.uid ?: run {
            _updateStatus.value = "Not logged in"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profileToSave = profile.copy(id = uid)
                repository.updateUserProfile(profileToSave)
                _updateStatus.value = "Profile updated successfully!"
                // Explicitly update local state to avoid flicker while waiting for sync
                _userProfile.value = profileToSave
            } catch (e: Exception) {
                e.printStackTrace()
                _updateStatus.value = "Save failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearStatus() {
        _updateStatus.value = null
    }
}
