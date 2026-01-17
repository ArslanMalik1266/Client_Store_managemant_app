package com.example.base44.dataClass

data class User(
    val email: String,
    val credit_limit: Double,
    val current_balance: Double,
    val commission_rate: Double,
    val weekly_commission: Double,
    val outstanding_debt: Double,
    val total_sales: Double,
    val total_commission: Double,
    val debt_overdue_days: Int,
    val last_commission_paid: Double?,
    val commission_paid_date: String?
)

