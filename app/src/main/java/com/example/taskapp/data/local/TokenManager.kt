package com.example.taskapp.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// Extension para usar DataStore
private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "token_preferences")

class TokenManager(private val context: Context) {

    companion object {
        private const val TAG = "TokenManager"
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")

        @Volatile
        private var INSTANCE: TokenManager? = null

        // También guardamos el último token en memoria para evitar bloqueos
        private var cachedToken: String? = null

        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                val instance = TokenManager(context)
                INSTANCE = instance
                instance
            }
        }
    }

    // Guardar token
    suspend fun saveToken(token: String) {
        try {
            Log.d(TAG, "Guardando token: ${token.takeLast(5)}")
            context.tokenDataStore.edit { preferences ->
                preferences[TOKEN_KEY] = token
            }
            // Actualizamos el caché en memoria
            cachedToken = token
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar token", e)
        }
    }

    // Eliminar token
    suspend fun deleteToken() {
        try {
            Log.d(TAG, "Eliminando token")
            context.tokenDataStore.edit { preferences ->
                preferences.remove(TOKEN_KEY)
            }
            // Limpiamos el caché
            cachedToken = null
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar token", e)
        }
    }

    // Observar token como Flow
    fun getTokenFlow(): Flow<String?> {
        return context.tokenDataStore.data.map { preferences ->
            preferences[TOKEN_KEY]?.also {
                // Actualizamos el caché cuando obtenemos un valor
                cachedToken = it
            }
        }
    }

    // Obtener token de forma síncrona (para el interceptor)
    fun getToken(): String? {
        // Primero verificamos el caché para una respuesta rápida
        if (cachedToken != null) {
            return cachedToken
        }

        // Si no tenemos el token en caché, lo intentamos obtener una vez
        return try {
            runBlocking {
                val token = context.tokenDataStore.data.first()[TOKEN_KEY]
                // Actualizamos el caché
                cachedToken = token
                token
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener token de manera síncrona", e)
            null
        }
    }
}