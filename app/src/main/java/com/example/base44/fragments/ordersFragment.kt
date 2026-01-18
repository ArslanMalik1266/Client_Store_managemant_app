package com.example.base44.fragments

import CheckResultsDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.MainActivity
import com.example.base44.R
import com.example.base44.adaptor.OrdersAdapter
import com.example.base44.dataClass.OrderItem
import com.example.base44.dataClass.isThisWeek
import com.example.base44.dataClass.isToday
import com.example.base44.dataClass.isWinner
import com.example.base44.dataClass.isYesterday
import com.example.base44.repository.OrderRepository
import com.example.base44.network.RetrofitClient
import com.example.base44.viewmodels.OrderViewModel
import com.example.base44.viewmodels.OrderViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ordersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrdersAdapter
    private var allOrders = listOf<OrderItem>()

    private lateinit var tvOrdersCount: TextView
    private lateinit var tvTotalSalesAmount: TextView
    private lateinit var totalOrdersTv: TextView
    private lateinit var viewModel: OrderViewModel

    private lateinit var chipToday: Chip
    private lateinit var chipYesterday: Chip
    private lateinit var chipThisWeek: Chip
    private lateinit var chipAll: Chip
    private lateinit var chipWinner: Chip
    private lateinit var chipGroupFilter: ChipGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_orders, container, false)

        setupToolbar(view)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = OrdersAdapter()
        recyclerView.adapter = adapter

        tvOrdersCount = view.findViewById(R.id.tvOrdersCount)
        tvTotalSalesAmount = view.findViewById(R.id.tvTotalSalesAmount)
        totalOrdersTv = view.findViewById(R.id.totalOrdersTv)

        setupChips(view)

        val repo = OrderRepository(RetrofitClient.api)
        val factory = OrderViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[OrderViewModel::class.java]

        observeViewModel()

        viewModel.loadOrders()

        chipAll.isChecked = true

        return view
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            view?.findViewById<android.widget.ProgressBar>(R.id.progressBar)?.visibility =
                if (isLoading) View.VISIBLE else View.GONE

            view?.findViewById<androidx.core.widget.NestedScrollView>(R.id.contentScrollView)?.visibility =
                if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.orders.observe(viewLifecycleOwner) { orderList ->
            allOrders = orderList.sortedByDescending { it.timestamp }
            filterOrders()
        }
    }

    private fun setupToolbar(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.topAppBar)
        toolbar.setNavigationIcon(R.drawable.back_arrow)
        toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
            requireActivity()
                .findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
                .selectedItemId = R.id.nav_home
        }

        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_check_results) {
                CheckResultsDialogFragment().show(parentFragmentManager, "check_results_dialog")
                true
            } else false
        }
    }

    private fun setupChips(view: View) {
        chipToday = view.findViewById(R.id.chipToday)
        chipYesterday = view.findViewById(R.id.chipYesterday)
        chipThisWeek = view.findViewById(R.id.chipThisWeek)
        chipAll = view.findViewById(R.id.chipAll)
        chipWinner = view.findViewById(R.id.chipWinner)
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter)

        chipGroupFilter.setOnCheckedChangeListener { _, _ ->
            filterOrders()
        }
    }

    private fun filterOrders() {
        val filtered = when {
            chipToday.isChecked -> allOrders.filter { it.isToday() }
            chipYesterday.isChecked -> allOrders.filter { it.isYesterday() }
            chipThisWeek.isChecked -> allOrders.filter { it.isThisWeek() }
            chipWinner.isChecked -> allOrders.filter { it.isWinner() }
            chipAll.isChecked -> allOrders
            else -> allOrders
        }
        adapter.submitList(filtered) // ListAdapter handles smooth updates automatically
        updateStats(filtered)
    }

    private fun updateStats(orderList: List<OrderItem>) {
        tvOrdersCount.text = orderList.size.toString()
        totalOrdersTv.text = "Total Check Orders = ${allOrders.size}"

        val total = orderList.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }
        tvTotalSalesAmount.text = "RM %.2f".format(total)

        val winningOrders = allOrders.filter { it.isWinner() }
        val winningTotal = winningOrders.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }

        view?.findViewById<TextView>(R.id.tvWinningAmount)?.text = "RM %.2f".format(winningTotal)
        view?.findViewById<TextView>(R.id.tvWinningStats)?.text = "Winning Orders = ${winningOrders.size}"
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.apply {
            toolbar.visibility = View.GONE
            enableDrawer(false)
        }
    }

    fun generateFinalInvoice(): String = "INV-${System.currentTimeMillis()}"
}
