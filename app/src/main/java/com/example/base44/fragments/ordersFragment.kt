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
import com.google.android.material.chip.Chip

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

    private val uid = "" // Placeholder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_orders, container, false)

        setupToolbar(view)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = OrdersAdapter(orders)
        recyclerView.adapter = adapter

        tvOrdersCount = view.findViewById(R.id.tvOrdersCount)
        tvTotalSalesAmount = view.findViewById(R.id.tvTotalSalesAmount)
        totalOrdersTv = view.findViewById(R.id.totalOrdersTv)

        setupChips(view)

        // Default selection must be BEFORE loading orders
        chipToday.isChecked = true



        return view
    }

    override fun onResume() {
        super.onResume()
        hideToolbarAndDrawer()
        loadOrdersFromApi()
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

        val chips = listOf(chipToday, chipYesterday, chipThisWeek, chipAll, chipWinner)
        chips.forEach { chip ->
            chip.setOnClickListener { filterOrders() }
        }
    }

    private fun loadOrdersFromApi() {
        val session = com.example.base44.adaptor.utils.SessionManager(requireContext())
        val userId = session.getUserId()

        if (userId.isNullOrEmpty()) {
             android.widget.Toast.makeText(context, "Please login to view orders", android.widget.Toast.LENGTH_SHORT).show()
             return
        }

        com.example.base44.network.RetrofitClient.instance.getOrders().enqueue(object : retrofit2.Callback<List<com.example.base44.dataClass.api.OrderEntity>> {
             override fun onResponse(
                 call: retrofit2.Call<List<com.example.base44.dataClass.api.OrderEntity>>,
                 response: retrofit2.Response<List<com.example.base44.dataClass.api.OrderEntity>>
             ) {
                 if (response.isSuccessful && response.body() != null) {
                     val allOrders = response.body()!!
                     val userEmail = session.getEmail()

                     // Client-side filtering (Check both ID and createdBy/Email)
                     // DEBUG LOGGING
                     android.util.Log.d("ORDERS_DEBUG", "Fetched ${allOrders.size} orders from API")
                     android.util.Log.d("ORDERS_DEBUG", "Current User ID: $userId, Email: $userEmail")
                     
                     // TEMPORARY DEBUG: Show ALL orders to verify fetch works
                     val userOrders = allOrders 
                     
                     /*
                     val userOrders = allOrders.filter { 
                         val matchId = (it.userId == userId)
                         val matchEmail = (!it.createdBy.isNullOrEmpty() && !userEmail.isNullOrEmpty() && it.createdBy == userEmail)
                         if (matchId || matchEmail) {
                             true 
                         } else {
                             // Log the first few that don't match to trace why
                             // android.util.Log.d("ORDERS_DEBUG", "Skipping Order: ID=${it.userId}, CreatedBy=${it.createdBy}")
                             false
                         }
                     }
                     */
                     android.util.Log.d("ORDERS_DEBUG", "Filtered down to ${userOrders.size} orders for user")

                     orders.clear()

                     val gson = com.google.gson.Gson()
                     val type = object : com.google.gson.reflect.TypeToken<List<com.example.base44.dataClass.add_to_cart_item>>() {}.type

                     userOrders.forEach { entity ->
                         try {
                              val cartItems: List<com.example.base44.dataClass.add_to_cart_item> = gson.fromJson(entity.itemsJson, type) ?: emptyList()

                              // Use the first item for summary
                              val firstItem = cartItems.firstOrNull()

                              // We need to flatten rows from all items if OrderItem expects a single list of rows,
                              // or just pass empty if checking results/history logic handles it differently.
                              // Assuming we just want to visual summary for now or aggregate rows.
                              val allRows = cartItems.flatMap { it.rows }

                              val item = OrderItem(
                                  invoiceNumber = entity.invoiceNumber ?: "",
                                  status = entity.status ?: "Pending",
                                  dateAdded = entity.createdDate ?: "",
                                  timestamp = parseTimestamp(entity.createdDate),
                                  totalAmount = entity.totalAmount?.toString() ?: "0.00",
                                  rows = allRows,
                                  productName = firstItem?.productName ?: "Multiple Items",
                                  productImage = firstItem?.drawableName ?: ""
                              )
                              orders.add(item)
                         } catch (e: Exception) {
                             android.util.Log.e("ORDERS_API", "Error parsing order items: ${e.message}")
                             // FALLBACK: Add the order anyway so it shows in the list
                             val fallbackItem = OrderItem(
                                  invoiceNumber = entity.invoiceNumber ?: "Unknown",
                                  status = entity.status ?: "Pending",
                                  dateAdded = entity.createdDate ?: "",
                                  timestamp = parseTimestamp(entity.createdDate),
                                  totalAmount = entity.totalAmount?.toString() ?: "0.00",
                                  rows = emptyList(),
                                  productName = "Order Details (Parse Error)",
                                  productImage = ""
                             )
                             orders.add(fallbackItem)
                         }
                     }

                     filterOrders() // Update UI

                 } else {
                     android.util.Log.e("ORDERS_API", "Failed to load orders: ${response.code()}")
                 }
             }

             override fun onFailure(call: retrofit2.Call<List<com.example.base44.dataClass.api.OrderEntity>>, t: Throwable) {
                 android.util.Log.e("ORDERS_API", "Network error loading orders", t)
             }
        })
    }

    private fun parseTimestamp(dateString: String?): Long {
        if (dateString.isNullOrEmpty()) return System.currentTimeMillis()

        // Try multiple formats
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )

        for (pattern in formats) {
            try {
                val format = java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault())
                // Adjust for timezone if needed, usually Z means UTC
                if (pattern.endsWith("'Z'")) {
                    format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val date = format.parse(dateString)
                if (date != null) return date.time
            } catch (e: Exception) {
                // Try next format
            }
        }

        return System.currentTimeMillis()
    }

    private fun filterOrders() {
        val filtered = when {
            chipToday.isChecked -> orders.filter { it.isToday() }
            chipYesterday.isChecked -> orders.filter { it.isYesterday() }
            chipThisWeek.isChecked -> orders.filter { it.isThisWeek() }
            chipAll.isChecked -> orders
            else -> orders
        }

        val filteredSorted = filtered.sortedByDescending { it.timestamp }

        adapter.updateData(filteredSorted)
        updateStats(filteredSorted)
    }


    private fun updateStats(orderList: List<OrderItem>) {
        tvOrdersCount.text = orderList.size.toString()
        totalOrdersTv.text = "Total Orders: ${orderList.size}"

        val total = orderList.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }
        tvTotalSalesAmount.text = "RM %.2f".format(total)
    }

    fun generateFinalInvoice(): String = "INV-${System.currentTimeMillis()}"
}
