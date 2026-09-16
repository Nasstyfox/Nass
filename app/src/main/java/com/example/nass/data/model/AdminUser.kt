package com.example.nass.data.model

import com.google.gson.annotations.SerializedName

data class AdminUser(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val role: String = "buyer",
    val status: String = "pending",
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)