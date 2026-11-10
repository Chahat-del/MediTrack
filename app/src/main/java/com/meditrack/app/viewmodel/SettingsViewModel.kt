package com.meditrack.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditrack.app.data.MediTrackRepository
import com.meditrack.app.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val displayName: String = "",
    val notificationsEnabled: Boolean = true
)

class SettingsViewModel(
    private val repository: MediTrackRepository
) : ViewModel() {

    val displayName: StateFlow<String> =
        repository.displayName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val notificationsEnabled: StateFlow<Boolean> =
        repository.notificationsEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val expiryWarningDays: StateFlow<Int> =
        repository.expiryWarningDays.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            repository.setDisplayName(name)
            _message.value = "Name updated"
        }
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
            if (enabled) {
                val medicines = repository.getAllMedicinesList()
                ReminderScheduler.restoreAllReminders(context, medicines)
                _message.value = "Notifications enabled"
            } else {
                for (med in repository.getAllMedicinesList()) {
                    ReminderScheduler.cancelMedicineReminders(context, med)
                }
                _message.value = "Notifications disabled"
            }
        }
    }

    fun setExpiryWarningDays(days: Int) {
        viewModelScope.launch {
            repository.setExpiryWarningDays(days)
            _message.value = "Expiry warning updated to $days days"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
