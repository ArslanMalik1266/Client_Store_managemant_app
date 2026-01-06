package com.example.base44.dataClass

data class add_to_cart_item(
    val productName: String,
    val bettingSlip: String? = null,
    val productCode: String,
    val raceDays: List<String> = emptyList(),
    val rows: List<CartRow>,
    val imageRes: Int,
    var totalAmount: Double = 0.0,
    var tempInvoice: String? = null,
    var finalInvoice: String? = null
)

data class CartRow(
    val number: String,
    val amount: String,
    val selectedCategories: List<String>,
    val qty: Int = 1
)
