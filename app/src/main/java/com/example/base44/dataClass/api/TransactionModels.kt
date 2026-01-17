package com.example.base44.dataClass.api

import com.google.gson.annotations.SerializedName

data class PaymentTransaction(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("user_email") val userEmail: String? = null,
    val type: String? = null, // 'credit_repayment', 'winning_payout', etc.
    val amount: Double? = null,
    val status: String? = null, // 'pending', 'paid', 'rejected'
    @SerializedName("payment_proof_url") val paymentProofUrl: String? = null,
    val notes: String? = null,
    val reference: String? = null,
    @SerializedName("created_date") val createdDate: String? = null
)

data class TransactionRequest(
    @SerializedName("user_email") val userEmail: String,
    val type: String,
    val amount: Double,
    val status: String,
    @SerializedName("payment_proof_url") val paymentProofUrl: String?,
    val notes: String?
)
