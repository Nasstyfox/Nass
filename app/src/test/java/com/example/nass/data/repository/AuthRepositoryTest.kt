package com.example.nass.data.repository

import com.example.nass.data.local.SessionManager
import com.example.nass.data.model.*
import com.example.nass.data.remote.ApiService
import com.example.nass.util.Logger
import com.example.nass.util.Resource
import com.google.gson.Gson

class AuthRepository(
    private val api: ApiService,
    private val session: SessionManager
) {
    private val gson = Gson()
    private val tag = "AuthRepo"

    suspend fun login(username: String, password: String): Resource<LoginResponse> {
        Logger.i(tag, "login() → username=$username")
        return try {
            val response = api.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                session.saveSession(
                    token = body.token,
                    userId = body.user.userId,
                    username = body.user.username,
                    email = body.user.email,
                    role = body.user.role
                )
                Logger.i(tag, "login success → role=${body.user.role}")
                Resource.Success(body)
            } else {
                val msg = parseError(response.errorBody()?.string(), response.code())
                Logger.w(tag, "login failed → $msg")
                Resource.Error(msg, response.code())
            }
        } catch (e: Exception) {
            Logger.e(tag, "login exception", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun register(req: RegisterRequest): Resource<RegisterResponse> {
        Logger.i(tag, "register() → username=${req.username} role=${req.role}")
        return try {
            val response = api.register(req)
            if (response.isSuccessful && response.body() != null) {
                Logger.i(tag, "register success → userId=${response.body()!!.userId}")
                Resource.Success(response.body()!!)
            } else {
                val msg = parseError(response.errorBody()?.string(), response.code())
                Logger.w(tag, "register failed → $msg")
                Resource.Error(msg, response.code())
            }
        } catch (e: Exception) {
            Logger.e(tag, "register exception", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun logout() {
        Logger.i(tag, "logout() — clearing DataStore")
        session.clearSession()
    }

    private fun parseError(raw: String?, code: Int): String {
        if (raw.isNullOrBlank()) return "HTTP $code"
        return try {
            gson.fromJson(raw, ApiError::class.java).message ?: raw.take(180)
        } catch (_: Exception) {
            raw.take(180)
        }
    }
}