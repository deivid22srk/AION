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
        private val API_KEY = stringPreferencesKey("api_key")
        private val SELECTED_MODEL = stringPreferencesKey("selected_model")
        private val FLOATING_LOG_ENABLED = booleanPreferencesKey("floating_log_enabled")
        
        const val DEFAULT_MODEL = "google/gemini-2.0-flash-exp:free"
    }
    
    val apiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_KEY] ?: ""
    }
    
    val selectedModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_MODEL] ?: DEFAULT_MODEL
    }
    
    val floatingLogEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FLOATING_LOG_ENABLED] ?: false
    }
    
    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }
    
    suspend fun saveSelectedModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_MODEL] = model
        }
    }
    
    suspend fun saveFloatingLogEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FLOATING_LOG_ENABLED] = enabled
        }
    }
    
    suspend fun getApiKeySync(): String {
        var key = ""
        context.dataStore.data.map { preferences ->
            key = preferences[API_KEY] ?: ""
        }
        return key
    }
}
