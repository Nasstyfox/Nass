package com.example.nass.data.repository

import com.example.nass.data.model.CreateProductRequest
import com.example.nass.data.model.Product
import com.example.nass.data.remote.ApiService
import com.example.nass.util.Resource
import retrofit2.Response

class ProductRepository(private val api: ApiService) {

    suspend fun create(req: CreateProductRequest): Resource<Product> =
        safeCall { api.createProduct(req) }

    suspend fun update(id: Int, req: CreateProductRequest): Resource<Product> =
        safeCall { api.updateProduct(id, req) }

    suspend fun delete(id: Int): Resource<Unit> {
        return try {
            val response = api.deleteProduct(id)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Error ${response.code()}", response.code())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = block()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Resource.Success(body)
            } else {
                Resource.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}