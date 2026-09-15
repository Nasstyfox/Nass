package com.example.nass.data.remote

import com.example.nass.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ============ AUTH ============
    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    // ============ SELLER ============
    @GET("api/v1/seller/stats")
    suspend fun getSellerStats(): Response<SellerStats>

    @GET("api/v1/seller/products")
    suspend fun getMyProducts(): Response<List<Product>>

    // ============ PRODUCTS ============
    @GET("api/v1/products/available")
    suspend fun getAvailableProducts(): Response<List<Product>>

    @POST("api/v1/products")
    suspend fun createProduct(@Body body: CreateProductRequest): Response<Product>

    @PUT("api/v1/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body body: CreateProductRequest
    ): Response<Product>

    @DELETE("api/v1/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Unit>
}