package com.example.base44.dataClass

data class OrderItem(
    val invoiceNumber: String = "",
    val status: String = "",
    val dateAdded: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val raceDay: String = "",
    val productImage: String = "",
    val productName: String = "",
    val productCode: String = "",
    val hashtag: String = "",
    val rows: List<CartRow> = emptyList(),
    val rmAmount: String = "",
    val totalLabel: String = "TOTAL",
    val totalAmount: String = "0"

)



