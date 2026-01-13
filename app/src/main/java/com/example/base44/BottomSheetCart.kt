package com.example.base44

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.adaptor.CartAdapter
import com.example.base44.adaptor.utils.generateFinalInvoice
import com.example.base44.dataClass.CartManager
import com.example.base44.dataClass.OrderItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
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

    private val uid = "" // Placeholder for user ID from session/token
    
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
        adapter = CartAdapter(CartManager.cartItems) { position ->
            CartManager.removeItem(position)
            adapter.notifyItemRemoved(position)
            updateTotal()
        }
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {

        btnClear.setOnClickListener {
            CartManager.clearCart()
            adapter.notifyDataSetChanged()
            updateTotal()
        }

        btnProceed.setOnClickListener {

            if (CartManager.cartItems.isEmpty()) return@setOnClickListener

            val totalBill = CartManager.cartItems.sumOf { it.totalAmount }

            // Check Balance
            if (availableBalance < totalBill) {
                android.widget.Toast.makeText(requireContext(), "Insufficient Balance! Please recharge your wallet.", android.widget.Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // TODO: Call API to place order and update balance
            // For now, simulate success:
            saveOrdersAndFinalize(totalBill)
        }
    }

    private fun saveOrdersAndFinalize(totalBill: Double) {
        val session = com.example.base44.adaptor.utils.SessionManager(requireContext())
        val userId = session.getUserId()
        
        if (userId.isNullOrEmpty()) {
             android.widget.Toast.makeText(requireContext(), "User session invalid. Please login again.", android.widget.Toast.LENGTH_LONG).show()
             return
        }
        
        btnProceed.isEnabled = false
        btnProceed.text = "Processing..."
        
        // Serialize items
        val gson = com.google.gson.Gson()
        val itemsJson = gson.toJson(CartManager.cartItems)
        val invoiceNum = "INV-${System.currentTimeMillis()}"
        
        val request = com.example.base44.dataClass.api.OrderRequest(
            userId = userId,
            invoiceNumber = invoiceNum,
            totalAmount = totalBill,
            status = "Pending",
            itemsJson = itemsJson
        )
        
        com.example.base44.network.RetrofitClient.instance.createOrder(request).enqueue(object : retrofit2.Callback<com.example.base44.dataClass.api.OrderEntity> {
            override fun onResponse(
                call: retrofit2.Call<com.example.base44.dataClass.api.OrderEntity>, 
                response: retrofit2.Response<com.example.base44.dataClass.api.OrderEntity>
            ) {
                 btnProceed.isEnabled = true
                 btnProceed.text = "Proceed"
                 
                if (response.isSuccessful) {
                    android.widget.Toast.makeText(requireContext(), "Order placed successfully!", android.widget.Toast.LENGTH_SHORT).show()
                    
                    // Clear Cart
                    CartManager.clearCart()
                    adapter.notifyDataSetChanged()
                    updateTotal()
                    
                    dismiss()
                    
                    // Go to Orders Page
                    requireActivity()
                        .findViewById<BottomNavigationView>(R.id.bottomNavigation)
                        .selectedItemId = R.id.nav_orders
                        
                } else {
                    val err = response.errorBody()?.string()
                    android.util.Log.e("ORDER_API", "Failed to create order: $err")
                    android.widget.Toast.makeText(requireContext(), "Failed to place order: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: retrofit2.Call<com.example.base44.dataClass.api.OrderEntity>, t: Throwable) {
                 btnProceed.isEnabled = true
                 btnProceed.text = "Proceed"
                 android.util.Log.e("ORDER_API", "Network error", t)
                 android.widget.Toast.makeText(requireContext(), "Network error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTotal() {
        val total = CartManager.cartItems.sumOf { it.totalAmount }
        totalText.text = "Total RM: %.2f".format(total)
    }
}
