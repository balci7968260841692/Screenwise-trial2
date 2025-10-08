package com.example.screentimemanager.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow

private const val PREFS_NAME = "screen_time_prefs"

val Context.dataStore by preferencesDataStore(name = PREFS_NAME)

object UserPreferenceKeys {
    val ONBOARDED = booleanPreferencesKey("onboarded")
    val DAILY_OVERRIDE_CAP = intPreferencesKey("daily_override_cap")
    val LAST_EXPORT = longPreferencesKey("last_export")
}

class UserPreferences(private val context: Context) {
    val data: Flow<Preferences> = context.dataStore.data

    suspend fun setOnboarded(value: Boolean) {
        context.dataStore.edit { it[UserPreferenceKeys.ONBOARDED] = value }
    }

    suspend fun setDailyOverrideCap(minutes: Int) {
        context.dataStore.edit { it[UserPreferenceKeys.DAILY_OVERRIDE_CAP] = minutes }
    }
}
