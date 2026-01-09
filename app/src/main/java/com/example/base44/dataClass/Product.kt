package com.example.base44.dataClass

data class Product(
    val code: String,
    val title: String,
    val drawableName: String = "",
    var isAddedToCart: Boolean = false
)
