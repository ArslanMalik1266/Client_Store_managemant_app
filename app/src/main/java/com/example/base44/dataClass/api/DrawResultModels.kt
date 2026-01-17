package com.example.base44.dataClass.api

import com.google.gson.annotations.SerializedName

data class DrawResult(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("product_id") val productId: String? = null,
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("draw_date") val drawDate: String? = null,
    @SerializedName("draw_number") val drawNumber: String? = null,
    @SerializedName("first_prize") val firstPrize: String? = null,
    @SerializedName("second_prize") val secondPrize: String? = null,
    @SerializedName("third_prize") val thirdPrize: String? = null,
    @SerializedName("special_prizes") val specialPrizes: List<String>? = null,
    @SerializedName("consolation_prizes") val consolationPrizes: List<String>? = null,
    @SerializedName("created_date") val createdDate: String? = null
)
