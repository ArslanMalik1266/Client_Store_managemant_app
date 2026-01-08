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
import com.example.base44.dataClass.OrdersManager
import com.example.base44.dataClass.add_to_cart_item
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BottomSheetCart(
    private val cartItems: List<add_to_cart_item>,
    private val availableBalance: Double
) : BottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnClear: Button
    private lateinit var btnProceed: Button
    private lateinit var totalText: TextView
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_cart, container, false)

        recyclerView = view.findViewById(R.id.recyclerView_cart)
        btnClear = view.findViewById(R.id.btnClearCart)
        btnProceed = view.findViewById(R.id.btnProceed)
        totalText = view.findViewById(R.id.tvTotalAmount)

        setupRecyclerView()
        setupButtons()
        updateTotal()
        btnClear()
        btnProceedClick()

        return view
    }

    private fun btnProceedClick() {
        btnProceed.setOnClickListener {
            if (!btnProceed.isEnabled || cartItems.isEmpty()) return@setOnClickListener

            val finalInvoice = generateFinalInvoice()
            val orderItems = CartManager.cartItems.map { cart ->
                OrderItem(
                    invoiceNumber = finalInvoice,
                    status = "Completed",
                    dateAdded = SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a",
                        Locale.getDefault()
                    ).format(Date()),
                    raceDay = cart.raceDays.lastOrNull() ?: SimpleDateFormat("EEE", Locale.getDefault()).format(Date()),
                    rows = cart.rows,
                    productImage = cart.imageRes,
                    productName = cart.productName,
                    productCode = cart.productCode,
                    hashtag = "#${System.currentTimeMillis().toString().takeLast(4)}",
                    rmAmount = "RM 2.00",
                    totalAmount = cart.totalAmount.toString()
                )
            }

            OrdersManager.addOrders(orderItems)
            CartManager.clearCart()
            dismiss()

            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavigation
            )
            bottomNav.selectedItemId = R.id.nav_orders
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CartAdapter(cartItems) { position ->
            CartManager.removeItem(position)
            adapter.notifyItemRemoved(position)
            updateTotal() // Update total after removal
        }
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        btnClear.setOnClickListener {
            CartManager.clearCart()
            adapter.notifyDataSetChanged()
            updateTotal()
        }
    }

    private fun btnClear() {
        btnClear.setOnClickListener {
            CartManager.clearCart()
            adapter.notifyDataSetChanged()
            updateTotal()
        }
    }

    private fun updateTotal() {
        val total = cartItems.sumOf { it.totalAmount }
        totalText.text = "Total RM: %.2f".format(total)
        validateBalance(total)
    }

    private fun validateBalance(total: Double) {
        if (availableBalance >= total) {
            btnProceed.isEnabled = true
            btnProceed.backgroundTintList = resources.getColorStateList(R.color.green, null)
            btnProceed.text = "Proceed"
        } else {
            btnProceed.isEnabled = false
            btnProceed.backgroundTintList = resources.getColorStateList(android.R.color.holo_red_light, null)
            btnProceed.text = "Insufficient Balance"
        }
    }
}
