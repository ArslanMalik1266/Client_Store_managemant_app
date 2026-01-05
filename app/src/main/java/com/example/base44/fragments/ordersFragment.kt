package com.example.base44.fragments

import CheckResultsDialogFragment
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
import com.example.base44.adaptor.OrdersAdapter
import com.example.base44.dataClass.OrderItem
import com.example.base44.dataClass.OrdersManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class ordersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val orders = mutableListOf<OrderItem>()

    override fun onResume() {
        super.onResume()
        hideToolbarAndDrawer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_orders, container, false)

        setupToolbar(view)
        initRecyclerView(view)
        orders.clear()
        orders.addAll(OrdersManager.orders)
        setupAdapter()

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

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_check_results -> {
                    val dialog = CheckResultsDialogFragment()
                    dialog.show(parentFragmentManager, "check_results_dialog")
                    true
                }
                else -> false
            }
        }
    }

    private fun initRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupAdapter() {
        val adapter = OrdersAdapter(orders)
        recyclerView.adapter = adapter
    }

    fun generateFinalInvoice(): String {
        return "INV-${System.currentTimeMillis()}"
    }
}
