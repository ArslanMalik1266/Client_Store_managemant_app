package com.example.base44.dataClass

object OrdersManager {

    private val orders = mutableListOf<OrderItem>()

    fun addOrders(list: List<OrderItem>) {
        orders.addAll(0, list)
    }
    fun getOrders(): List<OrderItem> {
        return orders
    }

    fun clear() {
        orders.clear()
    }
}
