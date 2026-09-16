package com.example.nass.data.model

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("newPassword") val newPassword: String
)