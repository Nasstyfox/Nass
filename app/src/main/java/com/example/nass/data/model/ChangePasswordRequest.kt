package com.example.nass.data.model

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)