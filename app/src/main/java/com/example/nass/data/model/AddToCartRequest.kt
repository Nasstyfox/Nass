package com.example.nass.data.model

import com.google.gson.annotations.SerializedName

data class AddToCartRequest(
    @SerializedName("product_id") val productId: Int
)