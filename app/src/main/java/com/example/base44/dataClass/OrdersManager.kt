package com.example.base44.dataClass

object OrdersManager {

    val orders = mutableListOf<OrderItem>()

    fun addOrders(list: List<OrderItem>) {
        orders.addAll(0, list)
    }

    fun clear() {
        orders.clear()
    }
}
