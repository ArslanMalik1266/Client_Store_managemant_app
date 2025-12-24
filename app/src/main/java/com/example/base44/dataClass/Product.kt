package com.example.base44.dataClass

data class Product(
    val code: String,
    val title: String,
    val imageRes: Int,
    var isAddedToCart: Boolean = false
)
