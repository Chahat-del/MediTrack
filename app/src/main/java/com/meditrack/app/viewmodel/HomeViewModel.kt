package com.meditrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditrack.app.data.DoseEntity
import com.meditrack.app.data.MediTrackRepository
import com.meditrack.app.data.MedicineEntity
import com.meditrack.app.util.DateTimeUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// UI state for the home dashboard
data class HomeUiState(
    val greeting: String = "",
    val todayDate: String = "",
    val medicines: List<MedicineEntity> = emptyList(),
    val todayDoses: List<DoseEntity> = emptyList(),
    val displayName: String = "",
    val takenCount: Int = 0,
    val remainingCount: Int = 0,
    val expiredCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val repository: MediTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getAllMedicines(),
                repository.getDosesForDate(DateTimeUtils.todayIso()),
                repository.displayName,
                repository.expiryWarningDays
            ) { medicines, doses, name, warningDays ->
                val today = DateTimeUtils.todayIso()
                val activeMedicines = medicines.filter { med ->
                    med.endDate == null || med.endDate >= today
                }
                val todaySchedule = activeMedicines.flatMap { med ->
                    med.reminderTimes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        .map { time ->
                            val dose = doses.find { it.medicineId == med.id && it.reminderTime == time }
                            med to (time to dose)
                        }
                }

                val taken = doses.count { it.status == "TAKEN" }
                val totalScheduled = todaySchedule.size
                val remaining = totalScheduled - taken

                // Expiry counts across all medicines
                val expired = medicines.count { med ->
                    med.expiryDate != null && med.expiryDate < today
                }
                val expiringSoon = medicines.count { med ->
                    val exp = med.expiryDate ?: return@count false
                    val days = DateTimeUtils.daysUntilExpiry(exp)
                    days in 0..warningDays
                }

                HomeUiState(
                    greeting = DateTimeUtils.getGreeting(),
                    todayDate = DateTimeUtils.formatDisplayDate(today),
                    medicines = activeMedicines,
                    todayDoses = doses,
                    displayName = name,
                    takenCount = taken,
                    remainingCount = remaining.coerceAtLeast(0),
                    expiredCount = expired,
                    expiringSoonCount = expiringSoon,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    // Mark a dose as Taken or Skipped, with deduplication
    fun recordDose(medicine: MedicineEntity, time: String, status: String) {
        viewModelScope.launch {
            val date = DateTimeUtils.todayIso()
            val existing = repository.getDose(medicine.id, time, date)
            if (existing != null) return@launch
            repository.recordDose(
                DoseEntity(
                    medicineId = medicine.id,
                    medicineName = medicine.name,
                    reminderTime = time,
                    date = date,
                    status = status
                )
            )
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            val today = DateTimeUtils.todayIso()

            // 8 realistic medicines covering every expiry state and medicine type
            val samples = listOf(
                // ── In-date medicines ─────────────────────────────────────────
                MedicineEntity(
                    name = "Paracetamol",
                    dosage = "500mg — 2 tablets",
                    instructions = "Take after meals. Max 8 tablets in 24 hrs.",
                    startDate = today,
                    endDate = null,
                    reminderTimes = "08:00,14:00,20:00",
                    expiryDate = DateTimeUtils.getNDaysFromNow(420),
                    quantity = 32,
                    medicineType = "Tablet"
                ),
                MedicineEntity(
                    name = "Salbutamol Inhaler",
                    dosage = "100mcg — 2 puffs",
                    instructions = "Use when breathless. Shake well before use.",
                    startDate = today,
                    endDate = null,
                    reminderTimes = "08:00,20:00",
                    expiryDate = DateTimeUtils.getNDaysFromNow(300),
                    quantity = 1,
                    medicineType = "Inhaler"
                ),
                MedicineEntity(
                    name = "Omeprazole",
                    dosage = "20mg — 1 capsule",
                    instructions = "Take 30 mins before breakfast.",
                    startDate = today,
                    endDate = null,
                    reminderTimes = "07:30",
                    expiryDate = DateTimeUtils.getNDaysFromNow(180),
                    quantity = 28,
                    medicineType = "Capsule"
                ),
                MedicineEntity(
                    name = "Lactulose Solution",
                    dosage = "15ml",
                    instructions = "With morning meal. Drink plenty of water.",
                    startDate = today,
                    endDate = null,
                    reminderTimes = "09:00",
                    expiryDate = DateTimeUtils.getNDaysFromNow(90),
                    quantity = 300,
                    medicineType = "Liquid"
                ),
                // ── Expiring soon (within 30 days) ────────────────────────────
                MedicineEntity(
                    name = "Vitamin D3",
                    dosage = "1000 IU — 1 tablet",
                    instructions = "Take with breakfast.",
                    startDate = today,
                    endDate = null,
                    reminderTimes = "09:00",
                    expiryDate = DateTimeUtils.getNDaysFromNow(18),
                    quantity = 20,
                    medicineType = "Tablet"
                ),
                MedicineEntity(
                    name = "Hydrocortisone Cream 1%",
                    dosage = "Thin layer",
                    instructions = "Apply to affected area twice daily. Avoid face.",
                    startDate = today,
                    endDate = DateTimeUtils.getNDaysFromNow(14),
                    reminderTimes = "08:00,20:00",
                    expiryDate = DateTimeUtils.getNDaysFromNow(22),
                    quantity = 1,
                    medicineType = "Cream/Ointment"
                ),
                // ── Expired ───────────────────────────────────────────────────
                MedicineEntity(
                    name = "Amoxicillin",
                    dosage = "250mg — 1 capsule",
                    instructions = "Complete full course. Every 8 hours with food.",
                    startDate = DateTimeUtils.getNDaysFromNow(-45),
                    endDate = DateTimeUtils.getNDaysFromNow(-38),
                    reminderTimes = "08:00,16:00,00:00",
                    expiryDate = DateTimeUtils.getNDaysFromNow(-8),
                    quantity = 6,
                    medicineType = "Capsule"
                ),
                MedicineEntity(
                    name = "Ibuprofen",
                    dosage = "400mg — 1 tablet",
                    instructions = "With or after food. Do not exceed 3 tablets in 24 hrs.",
                    startDate = DateTimeUtils.getNDaysFromNow(-120),
                    endDate = null,
                    reminderTimes = "08:00,20:00",
                    expiryDate = DateTimeUtils.getNDaysFromNow(-30),
                    quantity = 14,
                    medicineType = "Tablet"
                )
            )

            // Insert all medicines and collect the assigned IDs
            val insertedIds = mutableListOf<Long>()
            for (med in samples) {
                val id = repository.insertMedicine(med)
                insertedIds.add(id)
            }

            // Pre-record 2 disposal events (for the 2 expired medicines)
            // Amoxicillin disposal — pharmacy drop-off 5 days ago
            repository.recordDisposal(
                com.meditrack.app.data.DisposalEntity(
                    medicineId = insertedIds.getOrElse(6) { 0L },
                    medicineName = "Amoxicillin (old course)",
                    quantity = 10,
                    unit = "Capsule",
                    disposalMethod = "Pharmacy Drop-Off",
                    disposedDate = DateTimeUtils.getNDaysFromNow(-5),
                    timestamp = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000)
                )
            )
            // Ibuprofen partial disposal — at-home 12 days ago
            repository.recordDisposal(
                com.meditrack.app.data.DisposalEntity(
                    medicineId = insertedIds.getOrElse(7) { 0L },
                    medicineName = "Ibuprofen",
                    quantity = 8,
                    unit = "Tablet",
                    disposalMethod = "At-Home Safe Disposal",
                    disposedDate = DateTimeUtils.getNDaysFromNow(-12),
                    timestamp = System.currentTimeMillis() - (12L * 24 * 60 * 60 * 1000)
                )
            )
        }
    }
}
