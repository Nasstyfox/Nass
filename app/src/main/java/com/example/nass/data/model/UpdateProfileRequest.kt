package com.example.nass.data.model

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("display_name") val displayName: String?
)