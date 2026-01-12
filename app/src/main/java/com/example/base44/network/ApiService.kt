package com.example.base44.network

import com.example.base44.dataClass.api.AuthResponse
import com.example.base44.dataClass.api.LoginRequest
import com.example.base44.dataClass.api.RegisterRequest
import com.example.base44.dataClass.api.VerifyRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("auth/verify")
    fun verifyEmail(@Body request: VerifyRequest): Call<AuthResponse>
}
