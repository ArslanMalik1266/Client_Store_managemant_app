package com.example.base44.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.MainActivity
import com.example.base44.R
import com.example.base44.adaptor.SimpleOrdersAdapter
import com.example.base44.dataClass.SimpleOrderItem
import com.google.android.material.bottomnavigation.BottomNavigationView

class walletFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onResume() {
        super.onResume()
        hideToolbarAndDrawer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wallet, container, false)
        setupToolbar(view)
        setupRecyclerView(view)
        return view
    }


    private fun hideToolbarAndDrawer() {
        val activity = activity as MainActivity
        activity.toolbar.visibility = View.GONE
        activity.enableDrawer(false)
    }

    private fun setupToolbar(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.topAppBar)
        toolbar.setNavigationIcon(R.drawable.back_arrow)
        toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()

            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
            bottomNav.selectedItemId = R.id.nav_home
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)

        val simpleOrders = mutableListOf<SimpleOrderItem>().apply {
            repeat(15) {
                add(
                    SimpleOrderItem(
                        invoiceNumber = "INV 1766568425043",
                        dateAdded = "24 Dec 2025, 12:00 AM",
                        totalAmount = "RM 24.00",
                        status = "Completed"
                    )
                )
            }
        }

        val adapter = SimpleOrdersAdapter(simpleOrders) { position ->
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
}
