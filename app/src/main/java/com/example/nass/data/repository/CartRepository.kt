package com.example.nass.data.repository

import com.example.nass.data.model.AddToCartRequest
import com.example.nass.data.model.AddToCartResponse
import com.example.nass.data.model.CartItem
import com.example.nass.data.model.CheckoutResponse
import com.example.nass.data.model.TransactionSummary
import com.example.nass.data.remote.ApiService
import com.example.nass.util.Logger
import com.example.nass.util.Resource
import com.google.gson.Gson
import retrofit2.Response

class CartRepository(private val api: ApiService) {

    private val tag = "CartRepo"
    private val gson = Gson()

    suspend fun getCart(): Resource<List<CartItem>> {
        Logger.d(tag, "getCart()")
        return safeCall("getCart") { api.getCart() }
    }

    suspend fun addToCart(productId: Int): Resource<AddToCartResponse> {
        Logger.i(tag, "addToCart() -> productId=$productId")
        return safeCall("addToCart") { api.addToCart(AddToCartRequest(productId)) }
    }

    suspend fun removeFromCart(cartItemId: Int): Resource<Unit> {
        Logger.i(tag, "removeFromCart() -> cartItemId=$cartItemId")
        return try {
            val response = api.removeFromCart(cartItemId)
            if (response.isSuccessful) {
                Logger.d(tag, "removeFromCart ok")
                Resource.Success(Unit)
            } else {
                Logger.w(tag, "removeFromCart failed (${response.code()})")
                Resource.Error("Error ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Logger.e(tag, "removeFromCart exception", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun checkout(): Resource<CheckoutResponse> {
        Logger.i(tag, "checkout()")
        return try {
            val response = api.checkout()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Logger.i(tag, "checkout ok -> txId=${body.transactionId}")
                Resource.Success(body)
            } else {
                val msg = parseError(response.errorBody()?.string(), response.code())
                Logger.w(tag, "checkout failed -> $msg")
                Resource.Error(msg, response.code())
            }
        } catch (e: Exception) {
            Logger.e(tag, "checkout exception", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getHistory(): Resource<List<TransactionSummary>> {
        Logger.d(tag, "getHistory()")
        return safeCall("getHistory") { api.getTransactionHistory() }
    }

    private suspend fun <T> safeCall(label: String, block: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = block()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Logger.d(tag, "$label ok (${response.code()})")
                Resource.Success(body)
            } else {
                val msg = parseError(response.errorBody()?.string(), response.code())
                Logger.w(tag, "$label failed -> $msg")
                Resource.Error(msg, response.code())
            }
        } catch (e: Exception) {
            Logger.e(tag, "$label exception", e)
            Resource.Error(e.message ?: "Network error")
        }
    }

    private fun parseError(raw: String?, code: Int): String {
        if (raw.isNullOrBlank()) return "HTTP $code"
        return try {
            gson.fromJson(raw, com.example.nass.data.model.ApiError::class.java).message
                ?: raw.take(180)
        } catch (_: Exception) {
            raw.take(180)
        }
    }
}