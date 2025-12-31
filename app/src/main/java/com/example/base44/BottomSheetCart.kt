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
import com.example.base44.dataClass.CartItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetCart : BottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CartAdapter
    private lateinit var totalText: TextView
    private lateinit var btnProceed: Button
    private lateinit var btnClear: Button

    private val cartItems = mutableListOf<CartItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_cart, container, false)

        initViews(view)
        setupRecyclerView()
        setupButtons()

        return view
    }

    // ------------------- PRIVATE HELPERS -------------------

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView_cart)
        totalText = view.findViewById(R.id.tvTotalAmount)
        btnProceed = view.findViewById(R.id.btnProceed)
        btnClear = view.findViewById(R.id.btnClearCart)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)

        populateSampleCartItems()

        adapter = CartAdapter(cartItems) { position ->
            removeItem(position)
        }

        recyclerView.adapter = adapter
//        updateTotal()
    }

    private fun populateSampleCartItems() {
        repeat(15) {
            cartItems.add(
                CartItem(
                    R.drawable.headphones_image,
                    "Kuda",
                    "WH-1001",
                    "INV No: 25/12/T001",
                    "Thu 25/12/2025",
                    "#9706",
                    "RM 1.00"
                )
            )
        }
    }

    private fun setupButtons() {
        btnClear.setOnClickListener {
            clearCart()
        }

        btnProceed.setOnClickListener {
            proceedToCheckout()
        }
    }

    private fun removeItem(position: Int) {
        cartItems.removeAt(position)
        adapter.notifyItemRemoved(position)
//        updateTotal()
    }

    private fun clearCart() {
        cartItems.clear()
        adapter.notifyDataSetChanged()
//        updateTotal()
    }

    private fun proceedToCheckout() {
        // TODO: Handle checkout logic here
    }

//    private fun updateTotal() {
//        val total = cartItems.sumOf {
//            it.amount.replace("RM", "").trim().toDouble()
//        }
//        totalText.text = "Total RM: %.2f".format(total)
//    }
}
