package com.example.base44.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.base44.dataClass.Product
import com.example.base44.network.RetrofitClient
import com.example.base44.repository.ProductsRepository
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val repository: ProductsRepository = ProductsRepository(RetrofitClient.api)
) : ViewModel() {

    val productList: MutableLiveData<List<Product>> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData(false)
    val error: MutableLiveData<String> = MutableLiveData()

    fun loadProducts() {
        viewModelScope.launch {
            loading.value = true
            try {
                val response = repository.getProducts()

                if (response.isSuccessful && response.body() != null) {
                    val mappedList = response.body()!!.map { api ->
                        val numericId = api.code?.filter { it.isDigit() }?.toIntOrNull() ?: 0
                        Product(
                            productId = numericId,
                            code = api.code ?: api.id ?: "",
                            title = api.name ?: api.productTitle ?: "Unnamed Product",
                            drawableName = api.image ?: api.imageUrl ?: ""
                        )
                    }
                    productList.value = mappedList
                } else {
                    error.value = "API Error: ${response.code()}"
                }
            } catch (e: Exception) {
                error.value = "Exception: ${e.message}"
            } finally {
                loading.value = false
            }
        }
    }
}
