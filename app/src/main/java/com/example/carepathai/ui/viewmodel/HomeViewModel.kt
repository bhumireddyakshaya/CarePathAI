package com.example.carepathai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carepathai.data.local.entity.HealthHistory
import com.example.carepathai.data.local.entity.Medicine
import com.example.carepathai.domain.model.UserHealthProfile
import com.example.carepathai.domain.repository.AuthRepository
import com.example.carepathai.domain.repository.HealthHistoryRepository
import com.example.carepathai.domain.repository.HealthRepository
import com.example.carepathai.domain.repository.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HealthRepository,
    private val authRepository: AuthRepository,
    private val medicineRepository: MedicineRepository,
    private val historyRepository: HealthHistoryRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserHealthProfile())
    val userProfile: StateFlow<UserHealthProfile> = _userProfile

    val userMedicines: StateFlow<List<Medicine>> = medicineRepository.getAllMedicines()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val healthHistory: StateFlow<List<HealthHistory>> = historyRepository.getAllHistory()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
