package com.example.base44.dataClass

data class User_for_admin(
    val id: String,
    val fullName: String,
    val email: String,
    var currentBalance: Double,
    var creditLimit: Double = 0.0,
    var totalSales: Double = 0.0,
    var adminCredits: Int = 0,
    var canWork: Boolean = true,
    val role: String = "user"
)