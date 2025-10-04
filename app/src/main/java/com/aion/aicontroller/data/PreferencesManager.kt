package com.aion.aicontroller.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aion_preferences")

class PreferencesManager(private val context: Context) {
    
    companion object {
        private val API_KEY = stringPreferencesKey("api_key")
        private val SELECTED_MODEL = stringPreferencesKey("selected_model")
        
        const val DEFAULT_MODEL = "qwen/qwen2.5-vl-32b-instruct:free"
    }
    
    val apiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_KEY] ?: ""
    }
    
    val selectedModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_MODEL] ?: DEFAULT_MODEL
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
    
    suspend fun getApiKeySync(): String {
        var key = ""
        context.dataStore.data.map { preferences ->
            key = preferences[API_KEY] ?: ""
        }
        return key
    }
}
