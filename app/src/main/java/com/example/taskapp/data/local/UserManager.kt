package com.example.taskapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.taskapp.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Extension para usar DataStore
private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserManager(private val context: Context) {

    companion object {
        private val USER_KEY = stringPreferencesKey("current_user")
        private val json = Json { ignoreUnknownKeys = true }

        @Volatile
        private var INSTANCE: UserManager? = null

        fun getInstance(context: Context): UserManager {
            return INSTANCE ?: synchronized(this) {
                val instance = UserManager(context)
                INSTANCE = instance
                instance
            }
        }
    }

    // Guardar usuario
    suspend fun saveUser(user: User) {
        context.userDataStore.edit { preferences ->
            preferences[USER_KEY] = json.encodeToString(user)
        }
    }

    // Eliminar usuario
    suspend fun clearUser() {
        context.userDataStore.edit { preferences ->
            preferences.remove(USER_KEY)
        }
    }

    // Flow del usuario actual
    val currentUser: Flow<User?> = context.userDataStore.data.map { preferences ->
        val userString = preferences[USER_KEY]
        if (userString != null) {
            try {
                json.decodeFromString<User>(userString)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
}