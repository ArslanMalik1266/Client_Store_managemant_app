package com.example.base44.repository

import com.example.base44.dataClass.Order
import com.example.base44.dataClass.UploadResponse
import com.example.base44.dataClass.api.UserData
import com.example.base44.network.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call

class WalletRepository {

    private val api = RetrofitClient.instance

    // Fetch user profile (Retrofit Call)
    fun getUserProfile(userId: String): Call<UserData> {
        return api.getProfile(userId)
    }

    suspend fun getOrders(): retrofit2.Response<List<com.example.base44.dataClass.api.OrderRequestNew>> {
        return api.getOrders()
    }

    fun uploadFile(request: com.example.base44.dataClass.FileRequest): Call<okhttp3.ResponseBody> {
        return api.uploadFileBubble(request)
    }

    fun createPaymentTransaction(transaction: com.example.base44.dataClass.api.PaymentTransaction): Call<com.example.base44.dataClass.api.PaymentTransaction> {
        return api.createPaymentTransaction(transaction)
    }
}
