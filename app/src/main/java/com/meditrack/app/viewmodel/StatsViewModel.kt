package com.meditrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditrack.app.data.DisposalEntity
import com.meditrack.app.data.MediTrackRepository
import com.meditrack.app.data.MedicineEntity
import com.meditrack.app.util.DateTimeUtils
import kotlinx.coroutines.flow.*

data class StatsUiState(
    val totalMedicines: Int = 0,
    val expiredCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val inDateCount: Int = 0,
    val totalDisposals: Int = 0,
    val totalUnitsDisposed: Int = 0,
    val recentDisposals: List<DisposalEntity> = emptyList(),
    val disposalMethodBreakdown: Map<String, Int> = emptyMap(),
    val adherencePercent: Int = 0,
    val expiryWarningDays: Int = 30
)

class StatsViewModel(
    private val repository: MediTrackRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        repository.getAllMedicines(),
        repository.getAllDisposals(),
        repository.expiryWarningDays,
        repository.getAdherenceStats(
            DateTimeUtils.getNDaysAgoIso(30),
            DateTimeUtils.todayIso()
        )
    ) { medicines, disposals, warningDays, adherence ->
        val today = DateTimeUtils.todayIso()
        val expired = medicines.count { med ->
            med.expiryDate != null && med.expiryDate < today
        }
        val expiringSoon = medicines.count { med ->
            val exp = med.expiryDate ?: return@count false
            val days = DateTimeUtils.daysUntilExpiry(exp)
            days in 0..warningDays
        }
        val inDate = medicines.count { med ->
            val exp = med.expiryDate ?: return@count false
            DateTimeUtils.daysUntilExpiry(exp) > warningDays
        }

        val methodBreakdown = disposals.groupBy { it.disposalMethod }
            .mapValues { (_, list) -> list.size }

        val (taken, total) = adherence
        val adherencePct = if (total > 0) (taken * 100 / total) else 0

        StatsUiState(
            totalMedicines = medicines.size,
            expiredCount = expired,
            expiringSoonCount = expiringSoon,
            inDateCount = inDate,
            totalDisposals = disposals.size,
            totalUnitsDisposed = disposals.sumOf { it.quantity },
            recentDisposals = disposals.take(10),
            disposalMethodBreakdown = methodBreakdown,
            adherencePercent = adherencePct,
            expiryWarningDays = warningDays
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        StatsUiState()
    )
}
