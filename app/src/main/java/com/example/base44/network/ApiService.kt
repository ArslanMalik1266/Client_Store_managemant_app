package com.example.base44.network

import ResendOtpRequest
import com.example.base44.dataClass.api.AuthResponse
import com.example.base44.dataClass.api.LoginRequest
import com.example.base44.dataClass.api.ProductEntity
import com.example.base44.dataClass.api.RegisterRequest
import com.example.base44.dataClass.api.UserData
import com.example.base44.dataClass.api.VerifyRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("auth/verify-otp")
    fun verifyEmail(@Body request: VerifyRequest): Call<AuthResponse>

    @POST("auth/resend-otp")
    fun resendOtp(@Body request: ResendOtpRequest): Call<AuthResponse>

    @GET("entities/Product")
    fun getProducts(): Call<List<ProductEntity>>

    @GET("entities/User/{id}")
    fun getProfile(@retrofit2.http.Path("id") id: String): Call<UserData>

    @POST("entities/Order")
    fun createOrder(@Body order: com.example.base44.dataClass.api.OrderRequest): Call<com.example.base44.dataClass.api.OrderEntity>

    @GET("entities/Order")
    fun getOrders(): Call<List<com.example.base44.dataClass.api.OrderEntity>>
}
