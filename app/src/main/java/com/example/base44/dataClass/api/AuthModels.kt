package com.example.base44.dataClass.api

import com.google.gson.annotations.SerializedName

// Login Request
data class LoginRequest(
    val email: String,
    val password: String
)

// Register Request
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

// Verify Request
data class VerifyRequest(
    val email: String,
    val code: String
)

// Common Auth Response from Base44
data class AuthResponse(
    val status: String,
    val message: String?,
    val token: String?,
    val user: UserData?
)

data class UserData(
    val id: String?,
    @SerializedName("full_name") val fullName: String?,
    val username: String?,
    val email: String?,
    val role: String?
)
