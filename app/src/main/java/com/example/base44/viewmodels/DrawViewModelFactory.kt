package com.example.base44.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.base44.repository.DrawRepository

class DrawViewModelFactory(
    private val repo: DrawRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DrawViewModel::class.java)) {
            return DrawViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}