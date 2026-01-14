package com.example.base44.network

import com.example.base44.dataClass.api.ResendOtpRequest
import com.example.base44.dataClass.api.AuthResponse
import com.example.base44.dataClass.api.LoginRequest
import com.example.base44.dataClass.api.ProductEntity
import com.example.base44.dataClass.api.RegisterRequest
import com.example.base44.dataClass.api.UserData
import com.example.base44.dataClass.api.VerifyRequest
import com.example.base44.dataClass.api.PaymentTransaction
import com.example.base44.dataClass.api.TransactionRequest
import com.example.base44.dataClass.api.DrawResult
import com.example.base44.dataClass.api.WinningAmount
import com.example.base44.dataClass.api.FileUploadResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("auth/me")
    fun getMe(): Call<UserData>

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
    fun getOrders(@retrofit2.http.Query("sort") sort: String? = null): Call<List<com.example.base44.dataClass.api.OrderEntity>>

    // Payment Transactions
    @GET("entities/PaymentTransaction")
    fun getTransactions(
        @retrofit2.http.Query("user_email") email: String? = null,
        @retrofit2.http.Query("sort") sort: String? = "-created_date",
        @retrofit2.http.Query("limit") limit: Int? = 100
    ): Call<List<PaymentTransaction>>

    @POST("entities/PaymentTransaction")
    fun createTransaction(@Body transaction: TransactionRequest): Call<PaymentTransaction>

    // Draw Results
    @GET("entities/DrawResult")
    fun getDrawResults(
        @retrofit2.http.Query("sort") sort: String? = "-draw_date",
        @retrofit2.http.Query("limit") limit: Int? = 100
    ): Call<List<DrawResult>>

    // Winning Amounts
    @GET("entities/WinningAmount")
    fun getWinningAmounts(): Call<List<WinningAmount>>

    // File Upload
    @retrofit2.http.Multipart
    @POST("integrations/Core/UploadFile")
    fun uploadFile(@retrofit2.http.Part file: okhttp3.MultipartBody.Part): Call<FileUploadResponse>
}
