package com.example.base44.dataClass

import com.google.gson.annotations.SerializedName

data class add_to_cart_item(
    val productId: Int = 0,
    val productName: String,
    val bettingSlip: String? = null,
    val productCode: String,
    val raceDays: List<String> = emptyList(),
    val rows: List<CartRow>,
    val drawableName: String = "",
    var totalAmount: Double = 0.0,
    var tempInvoice: String? = null,
    var finalInvoice: String? = null,
    var walletBalance: Int = 0
)

data class CartRow(
    val number: String?,
    val amount: String,
    val selectedCategories: List<String>,
    val qty: Int = 1,
    )
