package com.example.base44.dataClass

data class User_for_admin(
    val id: String,
    val fullName: String,
    val email: String,
    var currentBalance: Int,
    var creditLimit: Int = 0,
    var totalSales: Int = 0,
    val role: String = "user"
)