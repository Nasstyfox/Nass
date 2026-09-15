package com.example.nass.data.repository

import com.example.nass.data.model.CreateProductRequest
import com.example.nass.data.model.Product
import com.example.nass.data.remote.ApiService
import com.example.nass.util.Resource

class ProductRepository(private val api: ApiService) {

    suspend fun create(req: CreateProductRequest): Resource<Product> = safeCall { api.createProduct(req) }

    suspend fun update(id: Int, req: CreateProductRequest): Resource<Product> = safeCall { api.updateProduct(id, req) }

    suspend fun delete(id: Int): Resource<Unit> = safeCall { api.deleteProduct(id) }

    private suspend fun <T> safeCall(block: suspend () -> retrofit2.Response<T>): Resource<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                val body = response.body()
                @Suppress("UNCHECKED_CAST")
                Resource.Success(body ?: Unit as T)
            } else {
                Resource.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}