package com.example.nass.data.model

import com.google.gson.annotations.SerializedName

data class CheckoutResponse(
    val message: String = "",
    @SerializedName("transactionId") val transactionId: Int = 0,
    @SerializedName("totalAmount") val totalAmount: Double = 0.0,
    @SerializedName("itemCount") val itemCount: Int = 0
)