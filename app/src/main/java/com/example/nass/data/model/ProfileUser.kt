package com.example.nass.data.model

import com.google.gson.annotations.SerializedName

data class ProfileUser(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    @SerializedName("display_name") val displayName: String? = null,
    val role: String = "buyer",
    val status: String = "pending"
)