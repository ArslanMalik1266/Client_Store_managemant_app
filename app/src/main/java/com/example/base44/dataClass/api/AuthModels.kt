package com.example.base44.dataClass.api

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

// Verify Request
data class VerifyRequest(
    val email: String,
    @SerializedName("otp_code") val code: String
)

// Common Auth Response from Base44
data class AuthResponse(
    val status: String?,
    val message: String?,
    val token: String?,
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("expires_in") val expiresIn: Int?,
    val user: UserData?
) {
    // Get the actual token (access_token or token)
    fun getActualToken(): String? = accessToken ?: token
}

data class UserData(
    val id: String?,
    @SerializedName("full_name") val fullName: String?,
    val username: String?,
    val email: String?,
    val role: String?,
    @SerializedName("current_balance") val currentBalance: Double? = 0.0,
    @SerializedName("credit_limit") val creditLimit: Double? = 0.0,
    @SerializedName("commission_rate") val commissionRate: Double? = 0.0
)
