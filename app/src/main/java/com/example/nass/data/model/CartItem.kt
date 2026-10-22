package com.example.nass.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "cached_cart")
data class CartItem(
    @PrimaryKey @SerializedName("cart_item_id") val cartItemId: Int = 0,
    @SerializedName("product_id") val productId: Int = 0,
    val name: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    val category: String = "Clothing",
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("seller_id") val sellerId: Int = 0,
    @SerializedName("seller_username") val sellerUsername: String? = null
)