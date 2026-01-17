package com.example.base44.repository


import com.example.base44.dataClass.api.OrderRequestNew
import com.example.base44.network.ApiService
import retrofit2.Response

class OrderRepository(private val api: ApiService) {

    suspend fun fetchOrders(): Response<List<OrderRequestNew>>  {
        return api.getOrders()
    }
}
