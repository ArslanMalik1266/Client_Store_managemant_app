package com.example.base44

import android.content.Intent
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
import com.example.base44.fragments.ordersFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BottomSheetCart(private val cartItems: List<add_to_cart_item>) : BottomSheetDialogFragment() {

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
        btnProceed()


        return view
    }

    private fun btnProceed() {
        btnProceed.setOnClickListener {

            if (cartItems.isEmpty()) return@setOnClickListener

            val finalInvoice = generateFinalInvoice()
            val orderItems = CartManager.cartItems.map { cart ->
                OrderItem(
                    invoiceNumber = finalInvoice,
                    status = "Completed",
                    dateAdded = SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a",
                        Locale.getDefault()
                    ).format(Date()),

                    raceDay = cart.raceDays.lastOrNull() ?: SimpleDateFormat(
                        "EEE",
                        Locale.getDefault()
                    ).format(
                        Date()
                    ),
                    rows = cart.rows,
                    productImage = cart.imageRes,
                    productName = cart.productName,
                    productCode = cart.productCode,
                    hashtag = "#${System.currentTimeMillis().toString().takeLast(4)}",
                    rmAmount = "RM 2.00",
                    totalAmount = "RM 24.00"

                )
            }

            OrdersManager.addOrders(orderItems)
            CartManager.clearCart()

            dismiss()

            val bottomNav =
                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    R.id.bottomNavigation
                )
            bottomNav.selectedItemId = R.id.nav_orders
        }
    }


    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CartAdapter(cartItems) { posiiton ->
            CartManager.removeItem(posiiton)
            adapter.notifyItemRemoved(posiiton)
        }
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        btnClear.setOnClickListener {
            val emptyList = cartItems.map { it.copy(rows = mutableListOf()) }
            adapter.updateData(emptyList)
            updateTotal()
        }

        btnProceed.setOnClickListener {
            // TODO: Checkout logic
        }
    }

    private fun btnClear() {
        btnClear.setOnClickListener {
            CartManager.clearCart()
            adapter.notifyDataSetChanged()
        }
    }

    private fun updateTotal() {
        val total = cartItems.sumOf { item ->
            item.rows.sumOf { row -> row.amount.toDoubleOrNull() ?: 0.0 }
        }
        totalText.text = "Total RM: %.2f".format(total)
    }
}
