package com.example.base44.dataClass

import java.text.SimpleDateFormat
import java.util.*

private val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

private fun String.toDate(): Date? {
    return try {
        sdf.parse(this)
    } catch (e: Exception) {
        null
    }
}

fun OrderItem.isToday(): Boolean {
    val calNow = Calendar.getInstance()
    val calOrder = Calendar.getInstance().apply { timeInMillis = this@isToday.timestamp }
    
    return calNow.get(Calendar.YEAR) == calOrder.get(Calendar.YEAR) &&
            calNow.get(Calendar.DAY_OF_YEAR) == calOrder.get(Calendar.DAY_OF_YEAR)
}

fun OrderItem.isYesterday(): Boolean {
    val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val calOrder = Calendar.getInstance().apply { timeInMillis = this@isYesterday.timestamp }

    return calYesterday.get(Calendar.YEAR) == calOrder.get(Calendar.YEAR) &&
            calYesterday.get(Calendar.DAY_OF_YEAR) == calOrder.get(Calendar.DAY_OF_YEAR)
}

fun OrderItem.isThisWeek(): Boolean {
    val calNow = Calendar.getInstance()
    val calOrder = Calendar.getInstance().apply { timeInMillis = this@isThisWeek.timestamp }

    return calNow.get(Calendar.YEAR) == calOrder.get(Calendar.YEAR) &&
            calNow.get(Calendar.WEEK_OF_YEAR) == calOrder.get(Calendar.WEEK_OF_YEAR)
}

fun OrderItem.isWinner(): Boolean {
    val s = this.status.lowercase()
    return s.contains("winner") || s.contains("won") || s.contains("winning")
}
