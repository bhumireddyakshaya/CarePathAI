package com.example.carepathai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carepathai.domain.model.UserHealthProfile
import com.example.carepathai.domain.repository.AuthRepository
import com.example.carepathai.domain.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HealthRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserHealthProfile())
    val userProfile: StateFlow<UserHealthProfile> = _userProfile

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        authRepository.currentUser?.uid?.let { uid ->
            viewModelScope.launch {
                repository.getUserProfile(uid).collect {
                    _userProfile.value = it
                }
            }
        }
    }
}
