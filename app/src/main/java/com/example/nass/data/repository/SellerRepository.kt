package com.example.nass.data.repository

import com.example.nass.data.model.Product
import com.example.nass.data.model.SellerStats
import com.example.nass.data.remote.ApiService
import com.example.nass.util.Logger
import com.example.nass.util.Resource
import retrofit2.Response

class SellerRepository(private val api: ApiService) {

    private val tag = "SellerRepo"

    suspend fun getStats(): Resource<SellerStats> {
        Logger.d(tag, "getStats()")
        return safeCall("getStats") { api.getSellerStats() }
    }

    suspend fun getMyProducts(): Resource<List<Product>> {
        Logger.d(tag, "getMyProducts()")
        return safeCall("getMyProducts") { api.getMyProducts() }
    }

    private suspend fun <T> safeCall(label: String, block: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = block()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Logger.d(tag, "$label ok (${response.code()})")
                Resource.Success(body)
            } else {
                Logger.w(tag, "$label failed (${response.code()})")
                Resource.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Logger.e(tag, "$label exception", e)
            Resource.Error(e.message ?: "Network error")
        }
    }
}