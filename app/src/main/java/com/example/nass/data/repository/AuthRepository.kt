package com.example.nass.data.repository

import com.example.nass.data.local.TokenManager
import com.example.nass.data.model.*
import com.example.nass.data.remote.ApiService
import com.example.nass.util.Resource
import com.google.gson.Gson

class AuthRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {
    private val gson = Gson()

    suspend fun login(username: String, password: String): Resource<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveSession(
                    token = body.token,
                    userId = body.user.userId,
                    username = body.user.username,
                    email = body.user.email,
                    role = body.user.role
                )
                Resource.Success(body)
            } else {
                Resource.Error(parseError(response.errorBody()?.string(), response.code()), response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun register(req: RegisterRequest): Resource<RegisterResponse> {
        return try {
            val response = api.register(req)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(parseError(response.errorBody()?.string(), response.code()), response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun logout() = tokenManager.clearSession()

    private fun parseError(raw: String?, code: Int): String {
        if (raw.isNullOrBlank()) return "HTTP $code"
        // If it's not JSON (ngrok page, gateway error, etc.), show a snippet
        return try {
            gson.fromJson(raw, ApiError::class.java).message
                ?: raw.take(180)
        } catch (_: Exception) {
            raw.take(180)
        }
    }
/*
    private fun parseError(raw: String?): String {
        if (raw.isNullOrBlank()) return "Unknown error"
        return try {
            gson.fromJson(raw, ApiError::class.java).message ?: "Unknown error"
        } catch (_: Exception) {
            "Unknown error"
        }
    }*/
}