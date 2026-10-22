package com.example.nass.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nass.data.model.CartItem
import com.example.nass.data.model.Product

@Dao
interface NassDao {
    // --- Products ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Query("SELECT * FROM cached_products")
    suspend fun getCachedProducts(): List<Product>

    @Query("DELETE FROM cached_products")
    suspend fun clearProducts()

    // --- Cart ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCart(cart: List<CartItem>)

    @Query("SELECT * FROM cached_cart")
    suspend fun getCachedCart(): List<CartItem>

    @Query("DELETE FROM cached_cart")
    suspend fun clearCart()
}