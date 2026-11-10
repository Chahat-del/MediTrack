package com.meditrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.meditrack.app.data.MediTrackRepository

/**
 * Custom ViewModelProvider.Factory that supplies the repository to all ViewModels.
 */
class MediTrackViewModelProvider(
    private val repository: MediTrackRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(repository) as T
            modelClass.isAssignableFrom(MedicineViewModel::class.java) ->
                MedicineViewModel(repository) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(repository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(repository) as T
            modelClass.isAssignableFrom(DisposalViewModel::class.java) ->
                DisposalViewModel(repository) as T
            modelClass.isAssignableFrom(StatsViewModel::class.java) ->
                StatsViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
