package com.example.nass.data.local

import android.content.Context
import kotlinx.coroutines.flow.first

class SessionStore(private val session: SessionManager) {

    suspend fun currentToken(): String? = session.tokenFlow.first()
    suspend fun currentRole(): String? = session.roleFlow.first()
    suspend fun clearSession() = session.clearSession()

    companion object {
        fun from(context: Context) = SessionStore(TokenManager(context.applicationContext))
    }
}