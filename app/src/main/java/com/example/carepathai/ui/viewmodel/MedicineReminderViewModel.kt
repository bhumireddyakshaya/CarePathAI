package com.example.carepathai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carepathai.data.local.entity.Medicine
import com.example.carepathai.domain.repository.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicineReminderViewModel @Inject constructor(
    private val repository: MedicineRepository
) : ViewModel() {

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val allMedicines: StateFlow<List<Medicine>> = repository.getAllMedicines()
        .catch { e ->
            e.printStackTrace()
            _statusMessage.value = "Firestore Error: ${e.localizedMessage ?: "Failed to fetch medicines"}"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMedicine(medicine: Medicine) {
        viewModelScope.launch {
            try {
                repository.insertMedicine(medicine)
                _statusMessage.value = "Medicine saved to Firebase successfully!"
            } catch (e: Exception) {
                e.printStackTrace()
                _statusMessage.value = "Save Failed: ${e.localizedMessage ?: "Failed to add medicine"}"
            }
        }
    }

    fun updateMedicine(medicine: Medicine) {
        viewModelScope.launch {
            try {
                repository.updateMedicine(medicine)
                _statusMessage.value = "Medicine status updated in Firebase!"
            } catch (e: Exception) {
                e.printStackTrace()
                _statusMessage.value = "Update Failed: ${e.localizedMessage ?: "Failed to update medicine"}"
            }
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            try {
                repository.deleteMedicine(medicine)
                _statusMessage.value = "Medicine deleted from Firebase successfully!"
            } catch (e: Exception) {
                e.printStackTrace()
                _statusMessage.value = "Delete Failed: ${e.localizedMessage ?: "Failed to delete medicine"}"
            }
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}
