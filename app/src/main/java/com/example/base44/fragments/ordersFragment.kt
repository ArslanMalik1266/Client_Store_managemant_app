package com.example.base44.fragments

import CheckResultsDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.MainActivity
import com.example.base44.R
import com.example.base44.adaptor.OrdersAdapter
import com.example.base44.dataClass.CartRow
import com.example.base44.dataClass.OrderItem
import com.example.base44.dataClass.isThisWeek
import com.example.base44.dataClass.isToday
import com.example.base44.dataClass.isYesterday
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ordersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrdersAdapter
    private val orders = mutableListOf<OrderItem>()

    private lateinit var tvOrdersCount: TextView
    private lateinit var tvTotalSalesAmount: TextView
    private lateinit var totalOrdersTv: TextView

    private lateinit var chipToday: Chip
    private lateinit var chipYesterday: Chip
    private lateinit var chipThisWeek: Chip
    private lateinit var chipAll: Chip
    private lateinit var chipWinner: Chip

    private val uid = FirebaseAuth.getInstance().uid ?: ""
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_orders, container, false)

        // Toolbar
        setupToolbar(view)

        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = OrdersAdapter(orders)
        recyclerView.adapter = adapter

        // Stats TextViews
        tvOrdersCount = view.findViewById(R.id.tvOrdersCount)
        tvTotalSalesAmount = view.findViewById(R.id.tvTotalSalesAmount)
        totalOrdersTv = view.findViewById(R.id.totalOrdersTv)

        // Chips
        setupChips(view)

        // Load orders from Firestore
        loadOrdersFromFirestore()

        return view
    }

    override fun onResume() {
        super.onResume()
        hideToolbarAndDrawer()
    }

    private fun hideToolbarAndDrawer() {
        val activity = activity as? MainActivity ?: return
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
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
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

        val chips = listOf(chipToday, chipYesterday, chipThisWeek, chipAll, chipWinner)
        chips.forEach { chip ->
            chip.setOnClickListener { filterOrders() }
        }
    }

    private fun loadOrdersFromFirestore() {
        if (uid.isEmpty()) return

        db.collection("users").document(uid)
            .collection("orders")
            .get()
            .addOnSuccessListener { result ->
                orders.clear()
                result.forEach { doc ->
                    // Map rows
                    val rowsList = (doc.get("rows") as? List<Map<String, Any>>)?.map { rowMap ->
                        CartRow(
                            number = rowMap["number"] as? String ?: "",
                            amount = rowMap["amount"] as? String ?: "",
                            selectedCategories = rowMap["selectedCategories"] as? List<String> ?: emptyList(),
                            qty = (rowMap["qty"] as? Long)?.toInt() ?: 1
                        )
                    } ?: emptyList()

                    // Map order
                    val order = OrderItem(
                        invoiceNumber = doc.getString("invoiceNumber") ?: "",
                        dateAdded = doc.getString("dateAdded") ?: "",
                        totalAmount = doc.getString("totalAmount") ?: "0",
                        status = doc.getString("status") ?: "",
                        productImage = doc.getString("productImage") ?: "",
                        productName = doc.getString("productName") ?: "",
                        productCode = doc.getString("productCode") ?: "",
                        rows = rowsList
                    )

                    orders.add(order)
                }

                adapter.updateData(orders)
                updateStats(orders)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load orders: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterOrders() {
        val filtered = when {
            chipToday.isChecked -> orders.filter { it.isToday() }
            chipYesterday.isChecked -> orders.filter { it.isYesterday() }
            chipThisWeek.isChecked -> orders.filter { it.isThisWeek() }
            chipAll.isChecked -> orders // no filter
            else -> orders
        }

        // If you want winner chip filtering uncomment
//        if (chipWinner.isChecked) {
//            filtered = filtered.filter { order ->
//                order.rows.any { it.isWinner }
//            }
//        }

        adapter.updateData(filtered)
        updateStats(filtered)
    }

    private fun updateStats(orderList: List<OrderItem>) {
        tvOrdersCount.text = orderList.size.toString()
        totalOrdersTv.text = "Total Check Orders = ${orders.size}"
        val total = orderList.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }
        tvTotalSalesAmount.text = "RM %.2f".format(total)
    }

    fun generateFinalInvoice(): String = "INV-${System.currentTimeMillis()}"
}
