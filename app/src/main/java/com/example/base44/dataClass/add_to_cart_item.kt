package com.example.base44.dataClass

data class add_to_cart_item(
    val productName: String,
    val bettingSlip: String? = null,
    val productCode: Int,
    val raceDays: List<String> = emptyList(),
    val rows: List<CartRow>,
    val drawableName: String = "",
    var totalAmount: Double = 0.0,
    var tempInvoice: String? = null,
    var finalInvoice: String? = null,
    var walletBalance: Int = 0
)

data class CartRow(
    val number: String,
    val amount: String,
    val selectedCategories: List<String>,
    val qty: Int = 1
)
