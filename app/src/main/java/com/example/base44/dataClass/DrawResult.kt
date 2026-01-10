package com.example.base44.dataClass

data class DrawResult(
    val product_name: String? = "",
    val draw_date: String? = "",
    val draw_number: String? = "",
    val first_prize: String? = "",
    val second_prize: String? = "",
    val third_prize: String? = "",
    val special_prizes: List<String>? = emptyList(),
    val consolation_prizes: List<String>? = emptyList()
)