package com.example.nass.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nass.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.PREFS_NAME)

class TokenManager(private val context: Context) : SessionManager {

    private val tokenKey = stringPreferencesKey(Constants.KEY_TOKEN)
    private val userIdKey = intPreferencesKey(Constants.KEY_USER_ID)
    private val usernameKey = stringPreferencesKey(Constants.KEY_USERNAME)
    private val emailKey = stringPreferencesKey(Constants.KEY_EMAIL)
    private val roleKey = stringPreferencesKey(Constants.KEY_ROLE)

    override val tokenFlow: Flow<String?> = context.dataStore.data.map { it[tokenKey] }
    override val roleFlow: Flow<String?> = context.dataStore.data.map { it[roleKey] }
    override val usernameFlow: Flow<String?> = context.dataStore.data.map { it[usernameKey] }

    override suspend fun saveSession(
        token: String,
        userId: Int,
        username: String,
        email: String,
        role: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = token
            prefs[userIdKey] = userId
            prefs[usernameKey] = username
            prefs[emailKey] = email
            prefs[roleKey] = role
        }
    }

    override suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}