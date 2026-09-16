package com.example.nass.data.repository

import com.example.nass.data.model.AdminUser
import com.example.nass.data.model.ApiError
import com.example.nass.data.model.MessageResponse
import com.example.nass.data.model.ResetPasswordRequest
import com.example.nass.data.model.UpdateUserRequest
import com.example.nass.data.remote.ApiService
import com.example.nass.util.Logger
import com.example.nass.util.Resource
import com.google.gson.Gson
import retrofit2.Response

class AdminRepository(private val api: ApiService) {

    private val tag = "AdminRepo"
    private val gson = Gson()

    suspend fun getAllUsers(): Resource<List<AdminUser>> {
        Logger.d(tag, "getAllUsers()")
        return safeCall("getAllUsers") { api.getAllUsers() }
    }

    suspend fun verifyUser(id: Int): Resource<MessageResponse> {
        Logger.i(tag, "verifyUser() -> id=$id")
        return safeCall("verifyUser") { api.verifyUser(id) }
    }

    suspend fun updateUser(id: Int, req: UpdateUserRequest): Resource<AdminUser> {
        Logger.i(tag, "updateUser() -> id=$id req=$req")
        return safeCall("updateUser") { api.updateUser(id, req) }
    }

    suspend fun deleteUser(id: Int): Resource<Unit> {
        Logger.i(tag, "deleteUser() -> id=$id")
        return try {
            val response = api.deleteUser(id)
            if (response.isSuccessful) {
                Logger.i(tag, "deleteUser ok")
                Resource.Success(Unit)
            } else {
                val msg = parseError(response.errorBody()?.string(), response.code())
                Logger.w(tag, "deleteUser failed -> $msg")
                Resource.Error(msg, response.code())
            }
        } catch (e: Exception) {
            Logger.e(tag, "deleteUser exception", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun resetPassword(id: Int, newPassword: String): Resource<MessageResponse> {
        Logger.i(tag, "resetPassword() -> id=$id")
        return safeCall("resetPassword") {
            api.resetPassword(id, ResetPasswordRequest(newPassword))
        }
    }

    private suspend fun <T> safeCall(label: String, block: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = block()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Logger.d(tag, "$label ok (${response.code()})")
                Resource.Success(body)
            } else {
                val msg = parseError(response.errorBody()?.string(), response.code())
                Logger.w(tag, "$label failed -> $msg")
                Resource.Error(msg, response.code())
            }
        } catch (e: Exception) {
            Logger.e(tag, "$label exception", e)
            Resource.Error(e.message ?: "Network error")
        }
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