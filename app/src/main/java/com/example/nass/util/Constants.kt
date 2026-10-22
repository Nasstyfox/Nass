package com.example.nass.util

object Constants {
    // Ngrok dev URL — swap for production Render/Railway URL before final demo
    const val BASE_URL = "https://upwind-defrost-jab.ngrok-free.dev"

    //Google web client ID for Android client
     //const val GOOGLE_WEB_CLIENT_ID = "619841473812-7nomb8buvsb3dtphnumimjvlf4sglbfp.apps.googleusercontent.com"
    const val GOOGLE_WEB_CLIENT_ID = "322786386187-l7qh5pv4ihmfq0lne1c0huksf912kqlo.apps.googleusercontent.com"
    // Cloudinary
    const val CLOUDINARY_CLOUD_NAME = "bxruni70"
    const val CLOUDINARY_UPLOAD_PRESET = "nass_uploads"
    const val CLOUDINARY_BASE_URL = "https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/"

    // API path prefixes
    const val API_PREFIX = "api/v1/"

    // DataStore keys
    const val PREFS_NAME = "nass_prefs"
    const val KEY_TOKEN = "jwt_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_EMAIL = "email"
    const val KEY_ROLE = "role"
}