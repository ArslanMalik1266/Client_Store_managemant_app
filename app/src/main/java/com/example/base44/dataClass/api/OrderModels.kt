package com.example.base44.dataClass.api

import com.google.gson.annotations.SerializedName

data class OrderRequestNew(
    @SerializedName("reference_number") val referenceNumber: String?,
    @SerializedName("customer_name") val customerName: String?,
    @SerializedName("user_name") val userName: String?,
    @SerializedName("selected_days") val selectedDays: List<String>?,
    @SerializedName("created_date") val createdDate: String?,
    @SerializedName("total_amount") val totalAmount: Double?,
    val status: String?,
    @SerializedName("items") val items: List<OrderRequestItem> = emptyList(),
)

data class OrderRequestItem(
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("product_code") val productCode: String? = null,
    @SerializedName("product_image") val productImage: String? = null,
    val numbers: String,
    @SerializedName("bet_type") val betType: String,
    @SerializedName("bet_amount") val betAmount: Double,
    val quantity: Int

)



