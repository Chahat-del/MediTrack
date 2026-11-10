package com.meditrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditrack.app.data.DisposalEntity
import com.meditrack.app.data.MediTrackRepository
import com.meditrack.app.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DisposalViewModel(
    private val repository: MediTrackRepository
) : ViewModel() {

    val allDisposals: StateFlow<List<DisposalEntity>> =
        repository.getAllDisposals().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> = _result.asStateFlow()

    fun clearResult() { _result.value = null }

    /**
     * Records a disposal event for a medicine.
     */
    fun recordDisposal(
        medicineId: Long,
        medicineName: String,
        quantity: Int,
        unit: String,
        method: String
    ) {
        viewModelScope.launch {
            try {
                repository.recordDisposal(
                    DisposalEntity(
                        medicineId = medicineId,
                        medicineName = medicineName,
                        quantity = quantity,
                        unit = unit,
                        disposalMethod = method,
                        disposedDate = DateTimeUtils.todayIso()
                    )
                )
                _result.value = "Disposal recorded for $medicineName"
            } catch (e: Exception) {
                _result.value = "Failed to record disposal: ${e.message}"
            }
        }
    }
}
