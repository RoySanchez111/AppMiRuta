package com.example.appbanco.logic

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {
    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_ID = intPreferencesKey("user_id")
        private val USERNAME = stringPreferencesKey("username")
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val PROFILE_IMAGE_URI = stringPreferencesKey("profile_image_uri")
    }

    suspend fun saveSession(userId: Int, username: String, token: String) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_ID] = userId
            preferences[USERNAME] = username
            preferences[AUTH_TOKEN] = token
        }
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val currentUserId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    val currentUsername: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME]
    }

    val profileImageUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PROFILE_IMAGE_URI]
    }

    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }

    suspend fun updateUsername(newUsername: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME] = newUsername
        }
    }

    suspend fun updateProfileImage(uriString: String?) {
        context.dataStore.edit { preferences ->
            if (uriString != null) {
                preferences[PROFILE_IMAGE_URI] = uriString
            } else {
                preferences.remove(PROFILE_IMAGE_URI)
            }
        }
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
