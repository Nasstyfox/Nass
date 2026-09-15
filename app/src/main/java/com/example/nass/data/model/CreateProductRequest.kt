package com.example.nass.data.model

import com.google.gson.annotations.SerializedName

data class CreateProductRequest(
    val name: String,
    val description: String?,
    val price: Double,
    val category: String,
    @SerializedName("image_url") val imageUrl: String?
)