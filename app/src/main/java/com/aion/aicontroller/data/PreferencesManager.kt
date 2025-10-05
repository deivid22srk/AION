package com.aion.aicontroller.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aion_preferences")

class PreferencesManager(private val context: Context) {
    
    companion object {
        private val SELECTED_LOCAL_MODEL = stringPreferencesKey("selected_local_model")
        private val FLOATING_LOG_ENABLED = booleanPreferencesKey("floating_log_enabled")
        
        const val DEFAULT_LOCAL_MODEL = "gemma3-1b-it"
    }
    
    val selectedLocalModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_LOCAL_MODEL] ?: DEFAULT_LOCAL_MODEL
    }
    
    val floatingLogEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FLOATING_LOG_ENABLED] ?: false
    }
    
    suspend fun saveSelectedLocalModel(modelId: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_LOCAL_MODEL] = modelId
        }
    }
    
    suspend fun saveFloatingLogEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FLOATING_LOG_ENABLED] = enabled
        }
    }
}
