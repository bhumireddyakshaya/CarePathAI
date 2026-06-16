package com.example.carepathai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carepathai.domain.model.UserHealthProfile
import com.example.carepathai.domain.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HealthRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserHealthProfile())
    val userProfile: StateFlow<UserHealthProfile> = _userProfile

    init {
        // Example initialization
        viewModelScope.launch {
            repository.getUserProfile("current_user_id").collect {
                _userProfile.value = it
            }
        }
    }
}
