package com.meditrack.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditrack.app.data.MedicineEntity
import com.meditrack.app.data.MediTrackRepository
import com.meditrack.app.notification.ReminderScheduler
import com.meditrack.app.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddMedicineResult(val success: Boolean, val message: String)

/** Expiry status for a medicine based on its expiryDate. */
enum class ExpiryStatus { EXPIRED, EXPIRING_SOON, IN_DATE, NO_EXPIRY }

data class MedicineWithExpiry(
    val medicine: MedicineEntity,
    val expiryStatus: ExpiryStatus,
    val daysUntilExpiry: Int?   // null when no expiry set
)

class MedicineViewModel(
    private val repository: MediTrackRepository
) : ViewModel() {

    // Live list of all medicines
    val medicines: StateFlow<List<MedicineEntity>> =
        repository.getAllMedicines().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Warning threshold from settings (default 30 days)
    private val expiryWarningDays: StateFlow<Int> =
        repository.expiryWarningDays.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    /** Medicines annotated with their expiry status, grouped for the inventory screen. */
    val medicinesWithExpiry: StateFlow<List<MedicineWithExpiry>> =
        kotlinx.coroutines.flow.combine(medicines, repository.expiryWarningDays) { meds, threshold ->
            meds.map { med ->
                val expiry = med.expiryDate
                if (expiry == null) {
                    MedicineWithExpiry(med, ExpiryStatus.NO_EXPIRY, null)
                } else {
                    val days = DateTimeUtils.daysUntilExpiry(expiry)
                    val status = when {
                        days < 0 -> ExpiryStatus.EXPIRED
                        days <= threshold -> ExpiryStatus.EXPIRING_SOON
                        else -> ExpiryStatus.IN_DATE
                    }
                    MedicineWithExpiry(med, status, days)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saveResult = MutableStateFlow<AddMedicineResult?>(null)
    val saveResult: StateFlow<AddMedicineResult?> = _saveResult.asStateFlow()

    fun clearSaveResult() {
        _saveResult.value = null
    }

    /**
     * Validates and adds a new medicine, then schedules its reminders.
     */
    fun addMedicine(
        context: Context,
        name: String,
        dosage: String,
        instructions: String,
        startDate: String,
        endDate: String?,
        reminderTimes: List<String>,
        expiryDate: String?,
        quantity: Int?,
        medicineType: String
    ) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _saveResult.value = AddMedicineResult(false, "Medicine name is required")
                return@launch
            }
            if (dosage.isBlank()) {
                _saveResult.value = AddMedicineResult(false, "Dosage is required")
                return@launch
            }
            if (reminderTimes.isEmpty()) {
                _saveResult.value = AddMedicineResult(false, "At least one reminder time is required")
                return@launch
            }
            if (startDate.isBlank()) {
                _saveResult.value = AddMedicineResult(false, "Start date is required")
                return@launch
            }
            if (endDate != null && endDate < startDate) {
                _saveResult.value = AddMedicineResult(false, "End date cannot be before start date")
                return@launch
            }

            try {
                val medicine = MedicineEntity(
                    name = name.trim(),
                    dosage = dosage.trim(),
                    instructions = instructions.trim(),
                    startDate = startDate,
                    endDate = endDate,
                    reminderTimes = reminderTimes.joinToString(","),
                    expiryDate = expiryDate,
                    quantity = quantity,
                    medicineType = medicineType
                )
                val id = repository.insertMedicine(medicine)
                val saved = medicine.copy(id = id)
                ReminderScheduler.scheduleMedicineReminders(context, saved)
                _saveResult.value = AddMedicineResult(true, "Medicine added successfully")
            } catch (e: Exception) {
                _saveResult.value = AddMedicineResult(false, "Failed to save: ${e.message}")
            }
        }
    }

    /**
     * Updates an existing medicine and reschedules its reminders.
     */
    fun updateMedicine(
        context: Context,
        medicine: MedicineEntity,
        name: String,
        dosage: String,
        instructions: String,
        startDate: String,
        endDate: String?,
        reminderTimes: List<String>,
        expiryDate: String?,
        quantity: Int?,
        medicineType: String
    ) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _saveResult.value = AddMedicineResult(false, "Medicine name is required")
                return@launch
            }
            if (dosage.isBlank()) {
                _saveResult.value = AddMedicineResult(false, "Dosage is required")
                return@launch
            }
            if (reminderTimes.isEmpty()) {
                _saveResult.value = AddMedicineResult(false, "At least one reminder time is required")
                return@launch
            }

            try {
                ReminderScheduler.cancelMedicineReminders(context, medicine)
                val updated = medicine.copy(
                    name = name.trim(),
                    dosage = dosage.trim(),
                    instructions = instructions.trim(),
                    startDate = startDate,
                    endDate = endDate,
                    reminderTimes = reminderTimes.joinToString(","),
                    expiryDate = expiryDate,
                    quantity = quantity,
                    medicineType = medicineType
                )
                repository.updateMedicine(updated)
                ReminderScheduler.scheduleMedicineReminders(context, updated)
                _saveResult.value = AddMedicineResult(true, "Medicine updated successfully")
            } catch (e: Exception) {
                _saveResult.value = AddMedicineResult(false, "Failed to update: ${e.message}")
            }
        }
    }

    /**
     * Deletes a medicine and cancels all its reminders.
     */
    fun deleteMedicine(context: Context, medicine: MedicineEntity) {
        viewModelScope.launch {
            try {
                ReminderScheduler.cancelMedicineReminders(context, medicine)
                repository.deleteMedicine(medicine)
            } catch (e: Exception) {
                _saveResult.value = AddMedicineResult(false, "Failed to delete: ${e.message}")
            }
        }
    }
}
