package com.example.base44.dataClass

data class CreateTransactionRequest(
    val amount: String,
    val transactionType: String,
    val userId: String,
    val description: String? = null
)
