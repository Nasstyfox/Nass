package com.example.nass.data.local

import android.content.Context
import kotlinx.coroutines.flow.first

class SessionStore(private val tokenManager: TokenManager) {

    suspend fun currentToken(): String? = tokenManager.tokenFlow.first()

    suspend fun currentRole(): String? = tokenManager.roleFlow.first()

    companion object {
        fun from(context: Context) = SessionStore(TokenManager(context.applicationContext))
    }
}