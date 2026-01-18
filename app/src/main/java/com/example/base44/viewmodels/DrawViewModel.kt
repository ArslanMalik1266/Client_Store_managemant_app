package com.example.base44.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.base44.dataClass.ResultItem
import com.example.base44.dataClass.api.DrawResult
import com.example.base44.repository.DrawRepository
import kotlinx.coroutines.launch

class DrawViewModel(private val repo: DrawRepository) : ViewModel() {

    private val _results = MutableLiveData<List<ResultItem>>()
    val results: LiveData<List<ResultItem>> = _results

    private val _availableDates = MutableLiveData<List<String>>()
    val availableDates: LiveData<List<String>> = _availableDates

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadResults() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("DrawViewModel", "Fetching draw results...")

                val apiResults: List<DrawResult> = repo.fetchDrawResults() // API call
                val uiResults: List<ResultItem> = apiResults.toResultItemList()

                _results.value = uiResults
                _error.value = null

                // Dynamic unique dates for filter
                val dates = apiResults.mapNotNull { it.drawDate }.distinct()
                _availableDates.value = dates

                Log.d("DrawViewModel", "Results loaded: ${uiResults.size}, Dates: ${dates.size}")

            } catch (e: Exception) {
                Log.e("DrawViewModel", "Error loading results: ${e.message}", e)
                _error.value = e.message ?: "Unknown error occurred"
                _results.value = emptyList()
                _availableDates.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// Extension function to map API model to UI model
fun List<DrawResult>.toResultItemList(): List<ResultItem> {
    return this.map { draw ->
        ResultItem(
            id = draw.id.toString(),
            title = draw.productName ?: "N/A",
            date = draw.drawDate ?: "",
            firstPrize = draw.firstPrize ?: "----",
            secondPrize = draw.secondPrize ?: "----",
            thirdPrize = draw.thirdPrize ?: "----",
            specialNumbers = draw.specialPrizes ?: emptyList(),
            consolationNumbers = draw.consolationPrizes ?: emptyList()
        )
    }
}
