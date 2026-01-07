package com.example.base44.fragments

import CheckResultsDialogFragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.MainActivity
import com.example.base44.R
import com.example.base44.adaptor.OrdersAdapter
import com.example.base44.dataClass.OrderItem
import com.example.base44.dataClass.OrdersManager
import com.example.base44.dataClass.isThisWeek
import com.example.base44.dataClass.isToday
import com.example.base44.dataClass.isYesterday
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip

class ordersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptor: OrdersAdapter
    private val orders = mutableListOf<OrderItem>()
    private lateinit var tvOrdersCount: TextView
    private lateinit var tvTotalSalesAmount: TextView
    private lateinit var totalOrdersTv: TextView
    private lateinit var chipGroup: com.google.android.material.chip.ChipGroup

    override fun onResume() {
        super.onResume()
        hideToolbarAndDrawer()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvOrdersCount = view.findViewById(R.id.tvOrdersCount)
        tvTotalSalesAmount = view.findViewById(R.id.tvTotalSalesAmount)
        totalOrdersTv = view.findViewById(R.id.totalOrdersTv)

        setupChips(view)

        // Initially update stats
        updateRecyclerViewAndStats(orders)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_orders, container, false)

        setupToolbar(view)
        initRecyclerView(view)
        orders.clear()
        orders.addAll(OrdersManager.getOrders())
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
      adaptor = OrdersAdapter(orders)
        recyclerView.adapter = adaptor
    }

    fun generateFinalInvoice(): String {
        return "INV-${System.currentTimeMillis()}"
    }
    private fun setupChips(view: View?) {
        val chipToday = view?.findViewById<Chip>(R.id.chipToday)
        val chipYesterday = view?.findViewById<Chip>(R.id.chipYesterday)
        val chipThisWeek = view?.findViewById<Chip>(R.id.chipThisWeek)
        val chipAll = view?.findViewById<Chip>(R.id.chipAll)
        val chipWinner = view?.findViewById<Chip>(R.id.chipWinner)

        val chips = listOf(chipToday, chipYesterday, chipThisWeek, chipAll, chipWinner)

        // single selection already set in XML (app:singleSelection="true")
        chips.forEach { chip ->
            chip?.setOnClickListener {
                filterOrders()
            }
        }
    }
    private fun filterOrders() {
        val chipToday = view?.findViewById<Chip>(R.id.chipToday)
        val chipYesterday = view?.findViewById<Chip>(R.id.chipYesterday)
        val chipThisWeek = view?.findViewById<Chip>(R.id.chipThisWeek)
        val chipAll = view?.findViewById<Chip>(R.id.chipAll)
        val chipWinner = view?.findViewById<Chip>(R.id.chipWinner)

        var filtered = OrdersManager.getOrders()

        when {
            chipToday?.isChecked == true -> filtered = filtered.filter { it.isToday() }
            chipYesterday?.isChecked == true -> filtered = filtered.filter { it.isYesterday() }
            chipThisWeek?.isChecked == true -> filtered = filtered.filter { it.isThisWeek() }
            chipAll?.isChecked == true -> {} // all, no filter
        }

//        if (chipWinner?.isChecked == true) {
//            filtered = filtered.filter { order ->
//                order.rows.any { row -> row.isWinner }  // assuming CartRow has isWinner
//            }
//        }

        updateRecyclerViewAndStats(filtered)
    }

    private fun updateRecyclerViewAndStats(orderList: List<OrderItem>) {
        // Update RecyclerView
        adaptor.updateData(orderList)
        // Update Orders Count
        tvOrdersCount.text = orderList.size.toString()
        totalOrdersTv.text = "Total Check Orders = ${orders.size}"

        // Total Sales calculation
        val total = orderList.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }
        tvTotalSalesAmount.text = "RM %.2f".format(total)
    }


}
