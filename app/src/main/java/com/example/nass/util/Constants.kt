package com.example.nass.util

object Constants {
    // Ngrok dev URL — swap for production Render/Railway URL before final demo
    const val BASE_URL = "https://upwind-defrost-jab.ngrok-free.dev/"

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