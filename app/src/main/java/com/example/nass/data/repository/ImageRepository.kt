package com.example.nass.data.repository

import android.content.Context
import android.net.Uri
import com.example.nass.data.remote.CloudinaryClient
import com.example.nass.util.Constants
import com.example.nass.util.Logger
import com.example.nass.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ImageRepository {

    private val tag = "ImageRepo"

    /**
     * Reads an image from the given [uri], uploads it to Cloudinary,
     * and returns the public HTTPS URL.
     *
     * Runs on IO. Compresses nothing — Cloudinary auto-optimises on delivery.
     */
    suspend fun uploadProductImage(context: Context, uri: Uri): Resource<String> =
        withContext(Dispatchers.IO) {
            try {
                Logger.i(tag, "uploadProductImage() -> uri=$uri")

                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext Resource.Error("Could not read image")

                if (bytes.isEmpty()) {
                    return@withContext Resource.Error("Image file is empty")
                }
                if (bytes.size > 8 * 1024 * 1024) {
                    return@withContext Resource.Error("Image too large (max 8 MB)")
                }

                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                val fileName = "product_${System.currentTimeMillis()}.${mime.substringAfterLast('/')}"

                val fileBody = bytes.toRequestBody(mime.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", fileName, fileBody)
                val presetBody = Constants.CLOUDINARY_UPLOAD_PRESET
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                val response = CloudinaryClient.getInstance().uploadImage(filePart, presetBody)
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    Logger.i(tag, "Upload ok -> ${body.secureUrl}")
                    Resource.Success(body.secureUrl)
                } else {
                    val err = response.errorBody()?.string()?.take(180) ?: "Upload failed"
                    Logger.w(tag, "Upload failed (${response.code()}): $err")
                    Resource.Error("Upload failed (${response.code()})", response.code())
                }
            } catch (e: Exception) {
                Logger.e(tag, "Upload exception", e)
                Resource.Error(e.message ?: "Upload failed")
            }
        }
}