package com.example.nass.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int = 0,
    @SerializedName("seller_id") val sellerId: Int = 0,
    val name: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    val category: String = "Clothing",
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("is_sold") val isSold: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null
)