package com.example.base44.dataClass

object CartManager {

    val cartItems = mutableListOf<add_to_cart_item>()

    var lastUserId: String? = null

    fun addItem(item: add_to_cart_item) {
        cartItems.add(0, item)
    }

    fun removeItem(position: Int) {
        if (position in cartItems.indices) {
            cartItems.removeAt(position)
        }
    }

    fun clearCart() {
        cartItems.clear()
    }
}
