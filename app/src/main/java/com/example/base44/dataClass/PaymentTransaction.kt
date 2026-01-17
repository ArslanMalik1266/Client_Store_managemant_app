package com.example.base44.dataClass

data class PaymentTransaction(
    val id: String,
    val type: String,
    val amount: Double,
    val status: String,
    val reference: String?,
    val created_date: String
)
