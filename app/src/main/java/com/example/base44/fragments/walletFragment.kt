package com.example.base44.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.adaptor.SimpleOrdersAdapter
import com.example.base44.dataClass.SimpleOrderItem


class walletFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val view = inflater.inflate(R.layout.fragment_wallet, container, false)


        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        val simpleOrders = mutableListOf<SimpleOrderItem>()
        simpleOrders.add(
            SimpleOrderItem(
                invoiceNumber = "INV 1766568425043",
                dateAdded = "24 Dec 2025, 12:00 AM",
                totalAmount = "RM 24.00",
                status = "Completed"
            )
        )
        simpleOrders.add(
            SimpleOrderItem(
                invoiceNumber = "INV 1766568425043",
                dateAdded = "24 Dec 2025, 12:00 AM",
                totalAmount = "RM 24.00",
                status = "Completed"
            )
        )
        simpleOrders.add(
            SimpleOrderItem(
                invoiceNumber = "INV 1766568425043",
                dateAdded = "24 Dec 2025, 12:00 AM",
                totalAmount = "RM 24.00",
                status = "Completed"
            )
        )
        simpleOrders.add(
            SimpleOrderItem(
                invoiceNumber = "INV 1766568425043",
                dateAdded = "24 Dec 2025, 12:00 AM",
                totalAmount = "RM 24.00",
                status = "Completed"
            )
        )
        simpleOrders.add(
            SimpleOrderItem(
                invoiceNumber = "INV 1766568425043",
                dateAdded = "24 Dec 2025, 12:00 AM",
                totalAmount = "RM 24.00",
                status = "Completed"
            )
        )
        val adapter = SimpleOrdersAdapter(simpleOrders) { position ->
            // Handle click if needed
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)



        return view
    }


}