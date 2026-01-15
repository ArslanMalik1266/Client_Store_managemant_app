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
import com.example.base44.dataClass.CartManager
import com.example.base44.dataClass.add_to_cart_item
import com.example.base44.dataClass.CartRow
import com.google.android.material.bottomnavigation.BottomNavigationView
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
        val session = com.example.base44.adaptor.utils.SessionManager(requireContext())
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

        // Prepare JSON for server directly from add_to_cart_item
        val itemsJson = CartManager.cartItems.flatMap { cartItem ->
            cartItem.rows.flatMap { row ->
                row.selectedCategories.map { category ->
                    mapOf(
                        "product_id" to cartItem.productCode.filter { it.isDigit() }.toIntOrNull(),
                        "product_code" to cartItem.productCode,
                        "product_name" to cartItem.productName,
                        "numbers" to row.number,
                        "bet_type" to category,
                        "bet_amount" to row.amount.toDoubleOrNull(),
                        "quantity" to row.qty,
                        "product_image" to cartItem.drawableName
                    )
                }
            }
        }

        val selectedDays = CartManager.cartItems.flatMap { it.raceDays }.distinct()
        val customerName = CartManager.cartItems.firstOrNull()?.productName ?: "Customer"
        val userName = session.getUsername() ?: customerName

        val orderRequest = mapOf(
            "reference_number" to invoiceNum,
            "customer_name" to customerName,
            "user_name" to userName,
            "selected_days" to if (selectedDays.isNotEmpty()) selectedDays else listOf(SimpleDateFormat("EEE").format(Date())),
            "order_date" to gmt8Date,
            "items" to itemsJson,
            "total_amount" to totalBill,
            "status" to "completed"
        )

        // Optional log for debugging 422 errors
        android.util.Log.d("ORDER_JSON", Gson().toJson(orderRequest))

        com.example.base44.network.RetrofitClient.instance.createOrderRaw(orderRequest)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    btnProceed.isEnabled = true
                    btnProceed.text = "Proceed"

                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show()

                        (activity as? com.example.base44.MainActivity)?.fetchUserProfile()
                        CartManager.clearCart()
                        adapter.updateData(CartManager.cartItems)
                        updateTotal()
                        dismiss()
                    } else {
                        val err = response.errorBody()?.string()
                        android.util.Log.e("ORDER_API", "Failed to create order: $err")
                        Toast.makeText(requireContext(), "Failed to place order: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    btnProceed.isEnabled = true
                    btnProceed.text = "Proceed"
                    android.util.Log.e("ORDER_API", "Network error", t)
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateTotal() {
        val total = CartManager.cartItems.sumOf { it.totalAmount }
        totalText.text = "Total RM: %.2f".format(total)
    }
}
