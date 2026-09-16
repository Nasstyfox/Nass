package com.example.nass.data.repository

import com.example.nass.data.model.ApiError
import com.example.nass.data.model.ChangePasswordRequest
import com.example.nass.data.model.GetProfileResponse
import com.example.nass.data.model.MessageResponse
import com.example.nass.data.model.ProfileUser
import com.example.nass.data.model.UpdateProfileRequest
import com.example.nass.data.remote.ApiService
import com.example.nass.util.Logger
import com.example.nass.util.Resource
import com.google.gson.Gson
import retrofit2.Response

class ProfileRepository(private val api: ApiService) {

    private val tag = "ProfileRepo"
    private val gson = Gson()

    suspend fun getProfile(): Resource<ProfileUser> {
        Logger.d(tag, "getProfile()")
        return try {
            val response = api.getProfile()
            val body: GetProfileResponse? = response.body()
            if (response.isSuccessful && body?.user != null) {
                Resource.Success(body.user)
            } else {
                Resource.Error(parseError(response.errorBody()?.string(), response.code()), response.code())
            }
        } catch (e: Exception) {
            Logger.e(tag, "getProfile exception", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateDisplayName(displayName: String?): Resource<ProfileUser> {
        Logger.i(tag, "updateDisplayName() -> $displayName")
        return try {
            val response = api.updateProfile(UpdateProfileRequest(displayName))
            val body = response.body()
            if (response.isSuccessful && body?.user != null) {
                Resource.Success(body.user)
            } else {
                Resource.Error(parseError(response.errorBody()?.string(), response.code()), response.code())
            }
        } catch (e: Exception) {
            Logger.e(tag, "updateDisplayName exception", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun updatePassword(currentPassword: String, newPassword: String): Resource<MessageResponse> {
        Logger.i(tag, "updatePassword()")
        return try {
            val response = api.updatePassword(ChangePasswordRequest(currentPassword, newPassword))
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Resource.Success(body)
            } else {
                Resource.Error(parseError(response.errorBody()?.string(), response.code()), response.code())
            }
        } catch (e: Exception) {
            Logger.e(tag, "updatePassword exception", e)
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