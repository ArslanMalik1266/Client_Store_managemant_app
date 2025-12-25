package com.example.base44.dataClass

data class OrderItem(
    val invoiceNumber: String,
    val status: String,
    val dateAdded: String,
    val raceDay: String,
    val productImage: Int,
    val productName: String,
    val productCode: String,
    val hashtag: String,
    val rmAmount: String,
    val totalLabel: String = "TOTAL",
    val totalAmount: String
)
