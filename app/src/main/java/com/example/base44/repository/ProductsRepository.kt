package com.example.base44.repository

import com.example.base44.dataClass.Product
import com.example.base44.dataClass.ProductEntity
import com.example.base44.network.ApiService
import retrofit2.Response

class ProductsRepository(private val api: ApiService) {

    suspend fun getProducts(): Response<List<ProductEntity>> {
        return api.getProducts()
    }
}
