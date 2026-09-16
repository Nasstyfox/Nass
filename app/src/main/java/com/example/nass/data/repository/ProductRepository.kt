package com.example.nass.data.repository

import com.example.nass.data.model.CreateProductRequest
import com.example.nass.data.model.Product
import com.example.nass.data.remote.ApiService
import com.example.nass.util.Logger
import com.example.nass.util.Resource
import retrofit2.Response

class ProductRepository(private val api: ApiService) {

    private val tag = "ProductRepo"

    suspend fun getAvailable(): Resource<List<Product>> {
        Logger.d(tag, "getAvailable()")
        return safeCall("getAvailable") { api.getAvailableProducts() }
    }

    suspend fun create(req: CreateProductRequest): Resource<Product> {
        Logger.i(tag, "create() -> name=${req.name} price=${req.price}")
        return safeCall("create") { api.createProduct(req) }
    }

    suspend fun update(id: Int, req: CreateProductRequest): Resource<Product> {
        Logger.i(tag, "update() -> id=$id name=${req.name}")
        return safeCall("update") { api.updateProduct(id, req) }
    }

    suspend fun delete(id: Int): Resource<Unit> {
        Logger.i(tag, "delete() -> id=$id")
        return try {
            val response = api.deleteProduct(id)
            if (response.isSuccessful) {
                Logger.i(tag, "delete ok")
                Resource.Success(Unit)
            } else {
                Logger.w(tag, "delete failed (${response.code()})")
                Resource.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Logger.e(tag, "delete exception", e)
            Resource.Error(e.message ?: "Network error")
        }
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