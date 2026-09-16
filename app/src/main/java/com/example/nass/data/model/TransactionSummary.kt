package com.example.nass.data.model

import com.google.gson.annotations.SerializedName

data class TransactionSummary(
    @SerializedName("transactionId") val transactionId: Int = 0,
    @SerializedName("totalAmount") val totalAmount: Double = 0.0,
    @SerializedName("createdAt") val createdAt: String? = null,
    val items: List<TransactionItem> = emptyList()
)