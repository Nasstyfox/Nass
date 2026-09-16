package com.example.nass.data.model

import com.google.gson.annotations.SerializedName

data class UpdateUserRequest(
    val role: String? = null,
    @SerializedName("display_name") val displayName: String? = null,
    val status: String? = null
)