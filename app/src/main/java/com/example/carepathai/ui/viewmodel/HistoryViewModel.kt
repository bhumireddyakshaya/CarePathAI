package com.example.carepathai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carepathai.data.local.entity.HealthHistory
import com.example.carepathai.domain.repository.HealthHistoryRepository
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
class HistoryViewModel @Inject constructor(
    private val repository: HealthHistoryRepository
) : ViewModel() {

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val allHistory: StateFlow<List<HealthHistory>> = repository.getAllHistory()
        .catch { e -> 
            e.printStackTrace()
            _statusMessage.value = "Firestore Error: ${e.localizedMessage ?: "Failed to fetch history"}"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteHistory(history: HealthHistory) {
        viewModelScope.launch {
            try {
                repository.deleteHistory(history)
                _statusMessage.value = "Deleted from Firebase successfully!"
            } catch (e: Exception) {
                e.printStackTrace()
                _statusMessage.value = "Delete Error: ${e.localizedMessage ?: "Failed to delete"}"
            }
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}
