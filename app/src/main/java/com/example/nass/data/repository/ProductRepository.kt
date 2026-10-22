package com.example.nass.data.repository

import com.example.nass.data.local.NassDao
import com.example.nass.data.model.CreateProductRequest
import com.example.nass.data.model.Product
import com.example.nass.data.remote.ApiService
import com.example.nass.util.Logger
import com.example.nass.util.Resource
import retrofit2.Response

class ProductRepository(
    private val api: ApiService,
    private val dao: NassDao // <-- Added DAO
) {

    private val tag = "ProductRepo"

    suspend fun getAvailable(): Resource<List<Product>> {
        Logger.d(tag, "getAvailable()")
        return try {
            val response = api.getAvailableProducts()
            if (response.isSuccessful && response.body() != null) {
                val products = response.body()!!
                // SYNC: Save fresh data to RoomDB
                dao.clearProducts()
                dao.insertProducts(products)
                Logger.d(tag, "getAvailable ok -> cached ${products.size} items")
                Resource.Success(products)
            } else {
                Resource.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Logger.e(tag, "getAvailable exception, loading from cache", e)
            // OFFLINE MODE: Load from RoomDB
            try {
                val cached = dao.getCachedProducts()
                if (cached.isNotEmpty()) {
                    Logger.i(tag, "Loaded ${cached.size} products from RoomDB cache")
                    Resource.Success(cached)
                } else {
                    Resource.Error("No internet and no cached data available.")
                }
            } catch (dbError: Exception) {
                Resource.Error("Network error and cache failed.")
            }
        }
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