package com.example.nass.data.local

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over session storage so tests can supply an in-memory fake.
 * Concrete implementation: [TokenManager] (DataStore-backed).
 */
interface SessionManager {
    val tokenFlow: Flow<String?>
    val roleFlow: Flow<String?>
    val usernameFlow: Flow<String?>

    suspend fun saveSession(
        token: String,
        userId: Int,
        username: String,
        email: String,
        role: String
    )

    suspend fun clearSession()
}