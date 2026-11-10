package com.meditrack.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Stores user preferences using Jetpack DataStore.
 * Keeps display name, notification toggle, onboarding-completed flag,
 * and the expiry warning threshold (how many days before expiry to alert).
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {

    companion object {
        private val DISPLAY_NAME = stringPreferencesKey("display_name")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val EXPIRY_WARNING_DAYS = intPreferencesKey("expiry_warning_days")
    }

    val displayName: Flow<String> = context.dataStore.data.map { it[DISPLAY_NAME] ?: "" }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_DONE] ?: false }
    val expiryWarningDays: Flow<Int> = context.dataStore.data.map { it[EXPIRY_WARNING_DAYS] ?: 30 }

    suspend fun setDisplayName(name: String) {
        context.dataStore.edit { it[DISPLAY_NAME] = name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[ONBOARDING_DONE] = done }
    }

    suspend fun setExpiryWarningDays(days: Int) {
        context.dataStore.edit { it[EXPIRY_WARNING_DAYS] = days }
    }
}
