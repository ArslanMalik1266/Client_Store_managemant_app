package com.example.base44.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.MainActivity
import com.example.base44.R
import com.example.base44.adaptor.SimpleOrdersAdapter
import com.example.base44.dataClass.OrdersManager
import com.example.base44.dataClass.SimpleOrderItem
import com.google.android.material.bottomnavigation.BottomNavigationView

class walletFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpleOrdersAdapter
    private lateinit var walletBalance: TextView
    private lateinit var availableBalance: TextView
    private lateinit var usedPercentText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onResume() {
        super.onResume()
        hideToolbarAndDrawer()
        loadOrders()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_wallet, container, false)
        walletBalance = view.findViewById(R.id.walletBalance)
        availableBalance = view.findViewById(R.id.availableBalance)
        usedPercentText = view.findViewById(R.id.usedPercentText)
        progressBar = view.findViewById(R.id.progressBar)
        setupToolbar(view)
        setupRecyclerView(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userWalletBalance = 5000.0
        val totalUsedAmount = OrdersManager.getOrders().sumOf { it.totalAmount.toDouble() }
        val currentAvailableBalance = userWalletBalance - totalUsedAmount

        walletBalance.text = "Credit Limit: RM $userWalletBalance"
        availableBalance.text = "RM $currentAvailableBalance"
        val percentUsed = ((totalUsedAmount / userWalletBalance) * 100).coerceIn(0.0, 100.0)
        usedPercentText.text = "%.1f%% used".format(percentUsed)
        progressBar.progress = percentUsed.toInt()

        // Update MainActivity's available balance
        (activity as? MainActivity)?.userAvailableBalance = currentAvailableBalance
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

            requireActivity()
                .findViewById<BottomNavigationView>(R.id.bottomNavigation)
                .selectedItemId = R.id.nav_home
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SimpleOrdersAdapter(emptyList())
        recyclerView.adapter = adapter
    }

    private fun loadOrders() {
        val orders = OrdersManager.getOrders()
        val simpleOrders = orders.map {
            SimpleOrderItem(
                invoiceNumber = it.invoiceNumber,
                dateAdded = it.dateAdded,
                totalAmount = "RM ${it.totalAmount}",
                status = it.status
            )
        }.reversed()
        adapter.updateData(simpleOrders)
    }
}
