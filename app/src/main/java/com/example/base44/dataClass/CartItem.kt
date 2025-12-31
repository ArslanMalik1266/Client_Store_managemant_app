package com.example.base44.dataClass

import java.io.Serializable

data class CartItem(
    val imageRes: Int,
    val name: String,
    val code: String,
    val invoiceNumber: String,
    val dateAdded: String,
    val hashtagCode: String,
    val rmCode: String
) : Serializable
