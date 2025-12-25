package com.example.base44.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.adaptor.OrdersAdapter
import com.example.base44.dataClass.OrderItem


class ordersFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_orders, container, false)


        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val orders = mutableListOf<OrderItem>()
        orders.add(
            OrderItem(
                invoiceNumber = "INV 1766568425043",
                status = "Completed",
                dateAdded = "24 Dec 2025, 12:00 AM",
                raceDay = "Wed",
                productImage = R.drawable.watch_image,
                productName = "Toto",
                productCode = "SW-2045",
                hashtag = "#7866",
                rmAmount = "RM 2.00",
                totalAmount = "RM 24.00"
            )
        )
        orders.add(
            OrderItem(
                invoiceNumber = "INV 1766568425043",
                status = "Completed",
                dateAdded = "24 Dec 2025, 12:00 AM",
                raceDay = "Wed",
                productImage = R.drawable.watch_image,
                productName = "Toto",
                productCode = "SW-2045",
                hashtag = "#7866",
                rmAmount = "RM 2.00",
                totalAmount = "RM 24.00"
            )
        )
        orders.add(
            OrderItem(
                invoiceNumber = "INV 1766568425043",
                status = "Completed",
                dateAdded = "24 Dec 2025, 12:00 AM",
                raceDay = "Wed",
                productImage = R.drawable.watch_image,
                productName = "Toto",
                productCode = "SW-2045",
                hashtag = "#7866",
                rmAmount = "RM 2.00",
                totalAmount = "RM 24.00"
            )
        )

        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = OrdersAdapter(orders)
        recyclerView.adapter = adapter

        return view

    }

}