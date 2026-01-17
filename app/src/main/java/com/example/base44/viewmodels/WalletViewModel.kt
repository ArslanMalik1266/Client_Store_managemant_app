package com.example.base44.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.base44.dataClass.Order
import com.example.base44.dataClass.OrderItem
import com.example.base44.dataClass.SimpleOrderItem
import com.example.base44.dataClass.UploadResponse
import com.example.base44.dataClass.api.UserData
import com.example.base44.dataClass.isThisWeek
import com.example.base44.dataClass.isToday
import com.example.base44.dataClass.isWinner
import com.example.base44.dataClass.isYesterday
import com.example.base44.repository.WalletRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class WalletViewModel(private val repository: WalletRepository) : ViewModel() {

    // -------------------------
    // LiveData for UI
    // -------------------------
    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> = _userData

    private val _orders = MutableLiveData<List<OrderItem>>()
    val orders: LiveData<List<OrderItem>> = _orders

    private val _simpleOrders = MutableLiveData<List<SimpleOrderItem>>()
    val simpleOrders: LiveData<List<SimpleOrderItem>> = _simpleOrders

    val uploadStatus = MutableLiveData<Boolean>()  // true if uploading
    val uploadResult = MutableLiveData<String>()

    private val _periodSales = MutableLiveData<Double>(0.0)
    val periodSales: LiveData<Double> = _periodSales

    private val _periodCommission = MutableLiveData<Double>(0.0)
    val periodCommission: LiveData<Double> = _periodCommission

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentFilter = OrderFilter.ALL

    fun uploadPaymentProof(
        userEmail: String,
        type: String = "credit_repayment",
        amount: Double,
        status: String = "pending",
        payment_proof_url: File,
        notes: String
    ) {
        uploadStatus.value = true

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // 1. Read and Encode File
                val bytes = payment_proof_url.readBytes()
                val base64Content = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                val fileName = payment_proof_url.name

                val fileRequest = com.example.base44.dataClass.FileRequest(name = fileName, content = base64Content)

                // 2. Upload File
                repository.uploadFile(fileRequest).enqueue(object : Callback<okhttp3.ResponseBody> {
                    override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {
                        if (response.isSuccessful) {
                            val fileUrl = response.body()?.string()
                            // If response is like "https://...", it's fine. If it is wrapped in quotes, strip them.
                            // Bubble usually returns raw URL text like: https://s3.amazonaws.com/...
                            // But let's log it to be sure.
                            Log.d("UploadProof", "File Uploaded. URL: $fileUrl")
                            
                            if (!fileUrl.isNullOrEmpty()) {
                                // 3. Create Transaction
                                createTransactionRecord(userEmail, type, amount, status, fileUrl, notes)
                            } else {
                                uploadStatus.postValue(false)
                                uploadResult.postValue("Upload failed: No URL returned")
                            }
                        } else {
                            uploadStatus.postValue(false)
                            uploadResult.postValue("Upload failed: ${response.code()} ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                        uploadStatus.postValue(false)
                        uploadResult.postValue("Upload failed: ${t.message}")
                    }
                })

            } catch (e: Exception) {
                uploadStatus.postValue(false)
                uploadResult.postValue("Error preparing file: ${e.message}")
            }
        }
    }

    private fun createTransactionRecord(
        userEmail: String,
        type: String,
        amount: Double,
        status: String,
        fileUrl: String,
        notes: String
    ) {
        // Clean URL if it has quotes (sometimes APIs return "http...")
        var cleanUrl = fileUrl.replace("\"", "").trim()
        if (cleanUrl.startsWith("//")) {
            cleanUrl = "https:$cleanUrl"
        }

        val transaction = com.example.base44.dataClass.api.PaymentTransaction(
            userEmail = userEmail,
            type = type,
            amount = amount,
            status = status,
            paymentProofUrl = cleanUrl,
            notes = notes
        )

        repository.createPaymentTransaction(transaction).enqueue(object : Callback<com.example.base44.dataClass.api.PaymentTransaction> {
            override fun onResponse(call: Call<com.example.base44.dataClass.api.PaymentTransaction>, response: Response<com.example.base44.dataClass.api.PaymentTransaction>) {
                uploadStatus.value = false
                if (response.isSuccessful) {
                    uploadResult.value = "Proof submitted successfully"
                } else {
                    uploadResult.value = "Submission failed: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<com.example.base44.dataClass.api.PaymentTransaction>, t: Throwable) {
                uploadStatus.value = false
                uploadResult.value = "Submission failed: ${t.message}"
            }
        })
    }


    // -------------------------
    // Fetch user profile
    // -------------------------
    fun fetchUserProfile(userId: String) {
        repository.getUserProfile(userId).enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                if (response.isSuccessful && response.body() != null) {
                    _userData.value = response.body()
                    // Re-calculate stats with new user data (commission rate)
                    filterOrders(currentFilter)
                }
            }
            override fun onFailure(call: Call<UserData>, t: Throwable) {}
        })
    }

    // -------------------------
    // Fetch Orders
    // -------------------------
    fun fetchOrders() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = repository.getOrders()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    
                    val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                    inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())

                    val mappedOrders = body.map { order ->
                        var timestamp = System.currentTimeMillis()
                        var formattedDate = order.createdDate ?: ""

                        try {
                            val date = inputFormat.parse(order.createdDate ?: "")
                            if (date != null) {
                                timestamp = date.time
                                formattedDate = outputFormat.format(date)
                            }
                        } catch (e: Exception) {
                            Log.e("WalletViewModel", "Date Parse Error: ${e.message}")
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
                            productImage = order.items.firstOrNull()?.productImage ?: ""
                        )
                    }
                    _orders.value = mappedOrders

                    // Initial display: ALL
                    filterOrders(OrderFilter.ALL)
                }
            } catch (e: Exception) {
                Log.e("WalletViewModel", "Error fetching orders: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // -------------------------
    // Filter orders by date
    // -------------------------
    fun filterOrders(filter: OrderFilter) {
        this.currentFilter = filter
        val allOrders = _orders.value ?: return
        val filtered = when (filter) {
            OrderFilter.TODAY -> allOrders.filter { it.isToday() }
            OrderFilter.YESTERDAY -> allOrders.filter { it.isYesterday() }
            OrderFilter.THIS_WEEK -> allOrders.filter { it.isThisWeek() }
            OrderFilter.WINNER -> allOrders.filter { it.isWinner() }
            OrderFilter.ALL -> allOrders
        }
        
        // Calculate Stats for the period
        val totalSales = filtered.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }
        val commissionRate = _userData.value?.commissionRate?.toDouble() ?: 0.0
        val totalCommission = totalSales * (commissionRate / 100.0)
        
        _periodSales.value = totalSales
        _periodCommission.value = totalCommission

        _simpleOrders.value = filtered.map {
            SimpleOrderItem(
                invoiceNumber = it.invoiceNumber,
                dateAdded = it.dateAdded,
                totalAmount = "RM %.2f".format(it.totalAmount.toDoubleOrNull() ?: 0.0),
                status = it.status
            )
        }
    }

    // -------------------------
    // Filters enum
    // -------------------------
    enum class OrderFilter {
        TODAY, YESTERDAY, THIS_WEEK, WINNER, ALL
    }
}
