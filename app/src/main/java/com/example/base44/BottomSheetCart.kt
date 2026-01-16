package com.example.base44

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.adaptor.CartAdapter
import com.example.base44.adaptor.utils.SessionManager
import com.example.base44.dataClass.CartManager
import com.example.base44.network.RetrofitClient
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class BottomSheetCart(
    private var availableBalance: Double
) : BottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnClear: Button
    private lateinit var btnProceed: Button
    private lateinit var totalText: TextView
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_cart, container, false)

        recyclerView = view.findViewById(R.id.recyclerView_cart)
        btnClear = view.findViewById(R.id.btnClearCart)
        btnProceed = view.findViewById(R.id.btnProceed)
        totalText = view.findViewById(R.id.tvTotalAmount)

        setupRecyclerView()
        setupButtons()
        updateTotal()

        return view
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CartAdapter(CartManager.cartItems.toMutableList()) { position ->
            CartManager.removeItem(position)
            adapter.updateData(CartManager.cartItems)
            updateTotal()
        }
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        btnClear.setOnClickListener {
            CartManager.clearCart()
            adapter.updateData(CartManager.cartItems)
            updateTotal()
        }

        btnProceed.setOnClickListener {
            if (CartManager.cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Cart is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalBill = CartManager.cartItems.sumOf { it.totalAmount }

            if (availableBalance < totalBill) {
                Toast.makeText(requireContext(), "Insufficient Balance! Please recharge your wallet.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            saveOrdersAndFinalize(totalBill)
        }
    }

    private fun saveOrdersAndFinalize(totalBill: Double) {
        val session = SessionManager(requireContext())
        val userId = session.getUserId()

        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User session invalid. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        btnProceed.isEnabled = false
        btnProceed.text = "Processing..."

        val invoiceNum = "INV-${System.currentTimeMillis()}"
        val gmt8Date = SimpleDateFormat("yyyy-MM-dd").apply {
            timeZone = TimeZone.getTimeZone("GMT+8")
        }.format(Date())

        val userName = session.getUsername() ?: "Customer"
        val customerName = userName
        val selectedDays = CartManager.cartItems.flatMap { it.raceDays }.distinct()

        // ONLY main product info (no rows)
        val itemsJson = CartManager.cartItems.map { cartItem ->
            mapOf(
                "product_id" to cartItem.productId,
                "product_code" to cartItem.productCode,
                "product_name" to cartItem.productName,
                "product_image" to cartItem.drawableName,
                "temp_invoice" to cartItem.tempInvoice.orEmpty(),
                "final_invoice" to cartItem.finalInvoice.orEmpty(),
                "total_amount" to cartItem.totalAmount
            )
        }

        val orderRequest = mapOf(
            "reference_number" to invoiceNum,
            "customer_name" to customerName,
            "user_name" to userName,
            "selected_days" to if (selectedDays.isNotEmpty()) selectedDays
            else listOf(SimpleDateFormat("EEE").format(Date())),
            "order_date" to gmt8Date,
            "items" to itemsJson,
            "total_amount" to totalBill,
            "status" to "completed"
        )

        val gson = Gson()
        android.util.Log.d("ORDER_DEBUG", "Order JSON: ${gson.toJson(orderRequest)}")

        // Send to server
        RetrofitClient.instance.createOrderRaw(orderRequest)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if (!isAdded || context == null) return

                    btnProceed.isEnabled = true
                    btnProceed.text = "Proceed"

                    if (response.isSuccessful) {
                        Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                        CartManager.clearCart()
                        adapter.updateData(CartManager.cartItems)
                        updateTotal()
                        dismiss()
                    } else {
                        val err = response.errorBody()?.string()
                        android.util.Log.e("ORDER_API", "Failed: $err")
                        Toast.makeText(context, "Failed to place order: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    if (!isAdded || context == null) return

                    btnProceed.isEnabled = true
                    btnProceed.text = "Proceed"
                    android.util.Log.e("ORDER_API", "Network error", t)
                    Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun updateTotal() {
        val total = CartManager.cartItems.sumOf { it.totalAmount }
        totalText.text = "Total RM: %.2f".format(total)
    }
}
