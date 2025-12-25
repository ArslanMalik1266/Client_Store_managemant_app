package com.example.base44

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.adaptor.CartAdapter
import com.example.base44.dataClass.CartItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.widget.Button
import android.widget.TextView

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

        recyclerView = view.findViewById(R.id.recyclerView_cart)
        totalText = view.findViewById(R.id.tvTotalAmount)
        btnProceed = view.findViewById(R.id.btnProceed)
        btnClear = view.findViewById(R.id.btnClearCart)

        recyclerView.layoutManager = LinearLayoutManager(context)

        // Sample items
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


        adapter = CartAdapter(cartItems) { position ->
            cartItems.removeAt(position)
            adapter.notifyItemRemoved(position)
//            updateTotal()
        }

        recyclerView.adapter = adapter
//        updateTotal()

        btnClear.setOnClickListener {
            cartItems.clear()
            adapter.notifyDataSetChanged()
//            updateTotal()
        }

        btnProceed.setOnClickListener {
            // Handle checkout logic here
        }

        return view
    }

//    private fun updateTotal() {
//        val total = cartItems.sumOf {
//            it.amount.replace("RM","").trim().toDouble()
//        }
//        totalText.text = "Total RM: %.2f".format(total)
//    }
}
