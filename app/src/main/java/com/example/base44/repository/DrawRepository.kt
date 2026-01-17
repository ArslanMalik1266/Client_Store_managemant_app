package com.example.base44.repository

import android.util.Log
import com.example.base44.dataClass.api.DrawResult
import com.example.base44.network.ApiService

class DrawRepository(private val api: ApiService) {
    suspend fun fetchDrawResults(): List<DrawResult> {
        return try {
            Log.d("DrawRepository", "Fetching draw results from API...")
            val response = api.getDrawResults()
            
            Log.d("DrawRepository", "Response Code: ${response.code()}")
            Log.d("DrawRepository", "Response Success: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val results = response.body()!!
                Log.d("DrawRepository", "Successfully fetched ${results.size} draw results")
                results
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("DrawRepository", "API Error - Code: ${response.code()}, Body: $errorBody")
                throw Exception("Failed to fetch draw results: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("DrawRepository", "Exception while fetching draw results", e)
            throw e
        }
    }
}