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
        // In the future, this data will be sent to the server via Retrofit
        // and the server will return the invoice/status details.
        
        // 3️⃣ CLEAR CART
        CartManager.clearCart()
        adapter.notifyDataSetChanged()

        android.widget.Toast.makeText(requireContext(), "Order placed successfully!", android.widget.Toast.LENGTH_SHORT).show()

        // Close bottom sheet
        dismiss()

        // Go to Orders Page
        requireActivity()
            .findViewById<BottomNavigationView>(R.id.bottomNavigation)
            .selectedItemId = R.id.nav_orders
    }

    private fun updateTotal() {
        val total = CartManager.cartItems.sumOf { it.totalAmount }
        totalText.text = "Total RM: %.2f".format(total)
    }
}
