package com.example.base44

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.base44.adaptor.CartAdapter
import com.example.base44.adaptor.utils.generateFinalInvoice
import com.example.base44.dataClass.CartManager
import com.example.base44.dataClass.OrderItem
import com.example.base44.dataClass.add_to_cart_item
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class BottomSheetCart(
    private val availableBalance: Double,
    private val onBalanceUpdated: ((newBalance: Double) -> Unit)? = null // callback to WalletFragment
) : BottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnClear: Button
    private lateinit var btnProceed: Button
    private lateinit var totalText: TextView
    private lateinit var adapter: CartAdapter

    private val uid = FirebaseAuth.getInstance().uid ?: ""
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

            val total = CartManager.cartItems.sumOf { it.totalAmount }

            // Fetch latest balance before proceeding
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val currentBalance = doc.getDouble("balance") ?: 0.0

                    if (currentBalance < total) {
                        btnProceed.isEnabled = false
                        btnProceed.text = "Insufficient Balance"
                        return@addOnSuccessListener
                    }

                    val finalInvoice = generateFinalInvoice()
                    val orderItems = CartManager.cartItems.map { cart ->
                        OrderItem(
                            invoiceNumber = finalInvoice,
                            status = "Completed",
                            dateAdded = SimpleDateFormat(
                                "dd MMM yyyy, hh:mm a",
                                Locale.getDefault()
                            ).format(Date()),
                            raceDay = cart.raceDays.lastOrNull()
                                ?: SimpleDateFormat("EEE", Locale.getDefault()).format(Date()),
                            rows = cart.rows,
                            productImage = cart.drawableName, // now accepts URI
                            productName = cart.productName,
                            productCode = cart.productCode,
                            hashtag = "#${System.currentTimeMillis().toString().takeLast(4)}",
                            rmAmount = "RM 2.00",
                            totalAmount = cart.totalAmount.toString()
                        )
                    }

                    // Save each order to Firestore
                    orderItems.forEach { order ->
                        db.collection("users").document(uid)
                            .collection("orders")
                            .add(order)
                    }

                    // Update balance
                    val newBalance = currentBalance - total
                    db.collection("users").document(uid).update("balance", newBalance)

                    // Notify WalletFragment
                    onBalanceUpdated?.invoke(newBalance)

                    // Clear cart and dismiss
                    CartManager.clearCart()
                    adapter.notifyDataSetChanged()
                    dismiss()

                    // Navigate to Orders tab
                    requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
                        .selectedItemId = R.id.nav_orders
                }
                .addOnFailureListener { e ->
                    btnProceed.isEnabled = true
                    btnProceed.text = "Proceed"
                    e.printStackTrace()
                }
        }
    }

    private fun updateTotal() {
        val total = CartManager.cartItems.sumOf { it.totalAmount }
        totalText.text = "Total RM: %.2f".format(total)
        validateBalance(total)
    }

    private fun validateBalance(total: Double) {
        if (CartManager.cartItems.isEmpty()) {
            btnProceed.isEnabled = false
            btnProceed.text = "Cart Emp"
        }
    }
}