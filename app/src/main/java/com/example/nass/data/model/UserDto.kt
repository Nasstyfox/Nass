package com.example.nass.data.model

data class UserDto(
    val userId: Int,
    val username: String,
    val email: String,
    val role: String,
    val status: String
)