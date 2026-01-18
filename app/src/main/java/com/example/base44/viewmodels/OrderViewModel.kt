package com.example.base44.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.base44.dataClass.CartRow
import com.example.base44.dataClass.OrderItem
import com.example.base44.repository.OrderRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch

class OrderViewModel(private val repo: OrderRepository) : ViewModel() {

    private val _orders = MutableLiveData<List<OrderItem>>()
    val orders: LiveData<List<OrderItem>> = _orders

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadOrders() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val response = repo.fetchOrders()

                if (response.isSuccessful) {

                    val body = response.body()
                    if (body != null) {

                        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                        inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())

                        val finalList = body.map { order ->

                            var timestamp = System.currentTimeMillis()
                            var formattedDate = order.createdDate ?: ""

                            try {
                                val date = inputFormat.parse(order.createdDate ?: "")
                                if (date != null) {
                                    timestamp = date.time
                                    formattedDate = outputFormat.format(date)
                                }
                            } catch (e: Exception) {
                                Log.e("ORDER_DEBUG", "Date Parse Error: ${e.message}")
                            }

                            val mappedRows: List<CartRow> = order.items.map { item ->
                                CartRow(
                                    number = item.numbers,
                                    amount = item.betAmount.toString(),
                                    selectedCategories = item.betType.split(","),
                                    qty = item.quantity
                                )
                            }

                            OrderItem(
                                invoiceNumber = order.referenceNumber ?: "",
                                dateAdded = formattedDate,
                                timestamp = timestamp,
                                totalAmount = order.totalAmount?.toString() ?: "0",
                                status = order.status ?: "",
                                raceDay = order.selectedDays?.joinToString(",") ?: "",
                                productName = order.items.firstOrNull()?.productName ?: "",
                                productCode = order.items.firstOrNull()?.productCode ?: "",
                                productImage = order.items.firstOrNull()?.productImage ?: "",
                                rows = mappedRows
                            )
                        }

                        _orders.value = finalList.sortedByDescending { it.timestamp }

                        Log.d("ORDER_DEBUG", "FINAL ORDERITEM LIST = ${Gson().toJson(finalList)}")
                    }
                }

            } catch (e: Exception) {
                Log.e("ORDER_DEBUG", "ERROR: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
