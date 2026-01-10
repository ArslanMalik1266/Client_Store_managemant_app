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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    private val uid = FirebaseAuth.getInstance().uid!!
    private val db = FirebaseFirestore.getInstance()

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
                btnProceed.isEnabled = false
                btnProceed.text = "Insufficient Balance"
                return@setOnClickListener
            }

            val newBalance = availableBalance - totalBill

            // 1️⃣ UPDATE FIRESTORE BALANCE
            db.collection("users").document(uid)
                .update("balance", newBalance)
                .addOnSuccessListener {
                    availableBalance = newBalance
                }

            // 2️⃣ SAVE ORDERS
            val invoice = generateFinalInvoice()
            val time = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

            CartManager.cartItems.forEach { cart ->
                val order = OrderItem(
                    invoiceNumber = invoice,
                    status = "Completed",
                    dateAdded = time,
                    raceDay = cart.raceDays.lastOrNull()
                        ?: SimpleDateFormat("EEE", Locale.getDefault()).format(Date()),
                    rows = cart.rows,
                    productImage = cart.drawableName,
                    productName = cart.productName,
                    productCode = cart.productCode,
                    hashtag = "#${System.currentTimeMillis().toString().takeLast(4)}",
                    rmAmount = "RM 2.00",
                    totalAmount = cart.totalAmount.toString()
                )

                db.collection("users").document(uid)
                    .collection("orders")
                    .add(order)
            }

            // 3️⃣ CLEAR CART
            CartManager.clearCart()
            adapter.notifyDataSetChanged()

            // Close bottom sheet
            dismiss()

            // Go to Orders Page
            requireActivity()
                .findViewById<BottomNavigationView>(R.id.bottomNavigation)
                .selectedItemId = R.id.nav_orders
        }
    }

    private fun updateTotal() {
        val total = CartManager.cartItems.sumOf { it.totalAmount }
        totalText.text = "Total RM: %.2f".format(total)
    }
}
