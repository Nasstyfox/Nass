package com.example.nass.data.repository

import com.example.nass.data.model.Product
import com.example.nass.data.model.SellerStats
import com.example.nass.data.remote.ApiService
import com.example.nass.util.Resource

class SellerRepository(private val api: ApiService) {

    suspend fun getStats(): Resource<SellerStats> = safeCall { api.getSellerStats() }

    suspend fun getMyProducts(): Resource<List<Product>> = safeCall { api.getMyProducts() }

    private suspend fun <T> safeCall(block: suspend () -> retrofit2.Response<T>): Resource<T> {
        return try {
            val response = block()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}