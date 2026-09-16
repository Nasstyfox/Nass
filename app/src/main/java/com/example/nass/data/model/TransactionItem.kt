package com.example.nass.data.model

import com.google.gson.annotations.SerializedName

data class TransactionItem(
    @SerializedName("productId") val productId: Int = 0,
    val name: String = "",
    @SerializedName("imageUrl") val imageUrl: String? = null,
    val category: String = "Clothing",
    @SerializedName("priceAtPurchase") val priceAtPurchase: Double = 0.0
)