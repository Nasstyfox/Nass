package com.example.nass.data.repository

import com.example.nass.util.Logger
import com.example.nass.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val tag = "FirestoreRepo"

    // Save a favorite to NoSQL
    suspend fun addFavorite(userId: String, productId: Int): Resource<Unit> {
        return try {
            val data = hashMapOf(
                "userId" to userId,
                "productId" to productId,
                "timestamp" to System.currentTimeMillis()
            )
            // Creates a document named "userId_productId"
            db.collection("favorites").document("${userId}_$productId").set(data).await()
            Logger.i(tag, "Added favorite: $productId for user $userId")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Logger.e(tag, "addFavorite exception", e)
            Resource.Error(e.message ?: "Failed to add favorite")
        }
    }

    // Remove a favorite
    suspend fun removeFavorite(userId: String, productId: Int): Resource<Unit> {
        return try {
            db.collection("favorites").document("${userId}_$productId").delete().await()
            Logger.i(tag, "Removed favorite: $productId")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Logger.e(tag, "removeFavorite exception", e)
            Resource.Error(e.message ?: "Failed to remove favorite")
        }
    }

    // Read favorites for a specific user
    suspend fun getFavorites(userId: String): Resource<List<Int>> {
        return try {
            val snapshot = db.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val productIds = snapshot.documents.mapNotNull { it.getLong("productId")?.toInt() }
            Logger.i(tag, "Fetched ${productIds.size} favorites")
            Resource.Success(productIds)
        } catch (e: Exception) {
            Logger.e(tag, "getFavorites exception", e)
            Resource.Error(e.message ?: "Failed to fetch favorites")
        }
    }
}