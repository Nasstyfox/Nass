package com.example.nass.data.remote

import com.example.nass.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Public endpoints that don't require auth
        val path = request.url.encodedPath
        val isPublic = path.contains("auth/login") || path.contains("auth/register")

        if (isPublic) return chain.proceed(request)

        // Read token from DataStore (blocking is acceptable in an Interceptor)
        val token = runBlocking { tokenManager.tokenFlow.first() }

        return if (!token.isNullOrBlank()) {
            val newRequest = request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(request)
        }
    }
}