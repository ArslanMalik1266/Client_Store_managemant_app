package com.example.base44.network

import com.example.base44.dataClass.CreateTransactionRequest
import com.example.base44.dataClass.Order
import com.example.base44.dataClass.OrderItem
import com.example.base44.dataClass.Product
import com.example.base44.dataClass.ProductEntity
import com.example.base44.dataClass.api.ResendOtpRequest
import com.example.base44.dataClass.api.AuthResponse
import com.example.base44.dataClass.api.DrawResult
import com.example.base44.dataClass.api.LoginRequest
import com.example.base44.dataClass.api.RegisterRequest
import com.example.base44.dataClass.api.UserData
import com.example.base44.dataClass.api.VerifyRequest
import com.example.base44.dataClass.api.PaymentTransaction
import com.example.base44.dataClass.api.OrderRequestNew
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Query

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
    suspend fun getProducts(): Response<List<ProductEntity>>

    @GET("entities/User/{id}")
    fun getProfile(@retrofit2.http.Path("id") id: String): Call<UserData>

    @PUT("entities/User/{id}")
    fun updateProfilePut(
        @retrofit2.http.Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<UserData>

    @POST("entities/Order")
    fun createOrderRaw(@Body orderData: Map<String, @JvmSuppressWildcards Any>): Call<Map<String, Any>>

    @POST("https://app.base44.com/fileupload")
    fun uploadFileBubble(@Body body: com.example.base44.dataClass.FileRequest): Call<okhttp3.ResponseBody>

    @POST("entities/PaymentTransaction")
    fun createPaymentTransaction(
        @Body body: PaymentTransaction
    ): Call<PaymentTransaction>

    @GET("entities/Order")
    suspend fun getOrders(): Response<List<OrderRequestNew>>

    @GET("entities/DrawResult")
    suspend fun getDrawResults(
        @Query("sort") sort: String = "-draw_date"
    ): Response<List<DrawResult>>

}

