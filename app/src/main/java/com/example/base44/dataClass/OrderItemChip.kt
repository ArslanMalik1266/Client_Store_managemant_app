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
    val orderDate = this.dateAdded.toDate() ?: return false
    return sdf.format(Date()) == sdf.format(orderDate)
}

fun OrderItem.isYesterday(): Boolean {
    val orderDate = this.dateAdded.toDate() ?: return false

    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -1)

    return sdf.format(cal.time) == sdf.format(orderDate)
}

fun OrderItem.isThisWeek(): Boolean {
    val orderDate = this.dateAdded.toDate() ?: return false

    val calNow = Calendar.getInstance()
    val weekNow = calNow.get(Calendar.WEEK_OF_YEAR)
    val yearNow = calNow.get(Calendar.YEAR)

    val calOrder = Calendar.getInstance()
    calOrder.time = orderDate

    return calOrder.get(Calendar.WEEK_OF_YEAR) == weekNow &&
            calOrder.get(Calendar.YEAR) == yearNow
}
