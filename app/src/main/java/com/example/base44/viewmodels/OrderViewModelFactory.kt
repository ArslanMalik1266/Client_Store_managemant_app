package com.example.base44.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.base44.repository.OrderRepository

class OrderViewModelFactory(
    private val repo: OrderRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
            return OrderViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
