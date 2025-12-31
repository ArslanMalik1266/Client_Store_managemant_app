package com.example.base44.dataClass

data class add_to_cart_item(
    val productName: String,
    val bettingSlip: String? = null,
    val raceDays: List<String> = emptyList(),
    val rows: List<CartRow>
)

data class CartRow(
    val number: String,
    val amount: String,
    val selectedCategories: List<String>,
)
