package com.example.nass.data.model

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val role: String  // "buyer" | "seller" | "admin"
)