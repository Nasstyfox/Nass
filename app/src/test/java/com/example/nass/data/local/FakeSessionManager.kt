package com.example.nass.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory implementation of [SessionManager] for unit tests. */
class FakeSessionManager : SessionManager {

    private val tokenState = MutableStateFlow<String?>(null)
    private val roleState = MutableStateFlow<String?>(null)

    override val tokenFlow: Flow<String?> = tokenState
    override val roleFlow: Flow<String?> = roleState

    override suspend fun saveSession(
        token: String,
        userId: Int,
        username: String,
        email: String,
        role: String
    ) {
        tokenState.value = token
        roleState.value = role
    }

    override suspend fun clearSession() {
        tokenState.value = null
        roleState.value = null
    }
}