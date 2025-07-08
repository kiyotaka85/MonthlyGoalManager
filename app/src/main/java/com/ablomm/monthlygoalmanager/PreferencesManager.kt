package com.ablomm.monthlygoalmanager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

class PreferencesManager(private val context: Context) {
    companion object {
        private val TIPS_HIDDEN_KEY = booleanPreferencesKey("tips_hidden")
        private val HIDE_COMPLETED_GOALS_KEY = booleanPreferencesKey("hide_completed_goals")
    }

    val isTipsHidden: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[TIPS_HIDDEN_KEY] ?: false
        }

    val isHideCompletedGoals: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HIDE_COMPLETED_GOALS_KEY] ?: false
        }

    suspend fun setTipsHidden(hidden: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TIPS_HIDDEN_KEY] = hidden
        }
    }

    suspend fun setHideCompletedGoals(hide: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIDE_COMPLETED_GOALS_KEY] = hide
        }
    }
}
