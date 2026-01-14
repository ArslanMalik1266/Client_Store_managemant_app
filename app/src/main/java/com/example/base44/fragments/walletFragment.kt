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
import com.example.base44.dataClass.CartRow
import com.example.base44.dataClass.OrderItem
import com.example.base44.dataClass.SimpleOrderItem
import com.example.base44.dataClass.isThisWeek
import com.example.base44.dataClass.isToday
import com.example.base44.dataClass.isYesterday
import com.example.base44.dataClass.api.UserData
import com.example.base44.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class walletFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpleOrdersAdapter
    private lateinit var walletBalance: TextView
    private lateinit var availableBalance: TextView
    private lateinit var usedPercentText: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var tvPeriodSaleAmount: TextView
    private lateinit var tvPeriodCommissionAmount: TextView
    private lateinit var chipToday: Chip
    private lateinit var chipYesterday: Chip
    private lateinit var chipThisWeek: Chip
    private lateinit var chipAll: Chip
    private lateinit var chipGroup: ChipGroup
    private lateinit var weekCommissionText: TextView
    private lateinit var creditDueWeekText: TextView

    private var creditLimit = 5000.0
    private var commissionRate = 25.0 // Default or from API
    var currentAvailableBalance = 0.0
    private var orders = listOf<OrderItem>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_wallet, container, false)

        // UI Init
        walletBalance = view.findViewById(R.id.walletBalance)
        availableBalance = view.findViewById(R.id.availableBalance)
        usedPercentText = view.findViewById(R.id.usedPercentText)
        progressBar = view.findViewById(R.id.progressBar)
        tvPeriodSaleAmount = view.findViewById(R.id.tvPeriodSaleAmount)
        tvPeriodCommissionAmount = view.findViewById(R.id.tvPeriodCommissionAmount)
        chipToday = view.findViewById(R.id.chipToday)
        chipYesterday = view.findViewById(R.id.chipYesterday)
        chipThisWeek = view.findViewById(R.id.chipThisWeek)
        chipAll = view.findViewById(R.id.chipAll)
        chipGroup = view.findViewById(R.id.chipGroupFilter)
        weekCommissionText = view.findViewById(R.id.weekCommission_id)
        creditDueWeekText = view.findViewById(R.id.creditDueWeek_text)

        setupToolbar(view)
        setupRecyclerView(view)
        setupChips()

        chipAll.isChecked = true

        return view
    }

    override fun onResume() {
        super.onResume()
        hideToolbarAndDrawer()
        fetchBalance()
        loadOrders()
    }

    // ------------------ PUBLIC FUNCTION TO REFRESH ORDERS & CREDIT ------------------
    fun refreshOrdersAndCredit() {
        fetchBalance()   // latest balance
        loadOrders()     // reload orders and update credit due
    }

    private fun fetchBalance() {
        val session = com.example.base44.adaptor.utils.SessionManager(requireContext())
        val userId = session.getUserId()
        
        android.util.Log.d("WALLET_API", "Fetching profile for User ID: $userId")
        
        if (userId.isNullOrEmpty()) {
             android.util.Log.e("WALLET_API", "User ID not found in session")
             return
        }

        RetrofitClient.instance.getProfile(userId).enqueue(object : Callback<com.example.base44.dataClass.api.UserData> {
            override fun onResponse(
                call: Call<com.example.base44.dataClass.api.UserData>,
                response: Response<com.example.base44.dataClass.api.UserData>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    creditLimit = user.creditLimit ?: 5000.0
                    currentAvailableBalance = user.currentBalance ?: 0.0
                    // Use commissionRate from API or default to 25.0 if null
                    commissionRate = user.commissionRate ?: 25.0 
                    updateBalanceUI()
                    // Re-calculate stats with new rate if orders are loaded
                    if (orders.isNotEmpty()) {
                         filterOrders()
                    }
                } else {
                     android.util.Log.e("WALLET_API", "Failed to fetch profile: ${response.code()} ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<com.example.base44.dataClass.api.UserData>, t: Throwable) {
                android.util.Log.e("WALLET_API", "Network error fetching profile", t)
            }
        })
    }

    // ------------------ CHIP FILTERS ------------------
    private fun setupChips() {
        listOf(chipToday, chipYesterday, chipThisWeek, chipAll).forEach { chip ->
            chip.setOnClickListener { filterOrders() }
        }
    }

    private fun filterOrders() {
        val filtered = when {
            chipToday.isChecked -> orders.filter { it.isToday() }
            chipYesterday.isChecked -> orders.filter { it.isYesterday() }
            chipThisWeek.isChecked -> orders.filter { it.isThisWeek() }
            chipAll.isChecked -> orders
            else -> orders
        }

        val simpleOrders = filtered.map { order ->
            SimpleOrderItem(
                invoiceNumber = order.invoiceNumber,
                dateAdded = order.dateAdded,
                totalAmount = "RM %.2f".format(order.totalAmount.toDoubleOrNull() ?: 0.0),
                status = order.status
            )
        }

        adapter.updateData(simpleOrders)
        updateStats(filtered)
        updateCreditDueWeek()
    }

    // ------------------ UPDATE STATS ------------------
    private fun updateStats(filteredOrders: List<OrderItem>) {
        val totalSales = filteredOrders.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }
        tvPeriodSaleAmount.text = "RM %.2f".format(totalSales)
        
        // Calculate commission
        val commission = totalSales * (commissionRate / 100.0)
        tvPeriodCommissionAmount.text = "RM %.2f".format(commission)

        val weekOrders = orders.filter { it.isThisWeek() }
        val weekTotal = weekOrders.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }
        val weekCommission = weekTotal * (commissionRate / 100.0)
        weekCommissionText.text = "RM %.2f".format(weekCommission)

        val nextMonday = getNextMonday()
        view?.findViewById<TextView>(R.id.comissionText)?.text = "Pay on $nextMonday"
    }

    private fun updateCreditDueWeek() {
        val creditUsedThisWeek = orders
            .filter { it.isThisWeek() }
            .sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }

        creditDueWeekText.text = "RM %.2f".format(creditUsedThisWeek)
    }

    private fun getNextMonday(): String {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val daysUntilMonday = (Calendar.MONDAY - today + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, if (daysUntilMonday == 0) 7 else daysUntilMonday)

        return SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()).format(calendar.time)
    }

    private fun updateBalanceUI() {
        walletBalance.text = "Credit Limit: RM %.2f".format(creditLimit)
        availableBalance.text = "RM %.2f".format(currentAvailableBalance)

        val used = creditLimit - currentAvailableBalance
        val percentUsed = ((used / creditLimit) * 100).coerceIn(0.0, 100.0)
        usedPercentText.text = "%.1f%% used".format(percentUsed)
        progressBar.progress = percentUsed.toInt()

        (activity as? MainActivity)?.userAvailableBalance = currentAvailableBalance
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SimpleOrdersAdapter(emptyList())
        recyclerView.adapter = adapter
    }

    private fun loadOrders() {
        val session = com.example.base44.adaptor.utils.SessionManager(requireContext())
        val userId = session.getUserId()
        val userEmail = session.getEmail()

        if (userId.isNullOrEmpty()) return

        RetrofitClient.instance.getOrders().enqueue(object : Callback<List<com.example.base44.dataClass.api.OrderEntity>> {
             override fun onResponse(
                 call: Call<List<com.example.base44.dataClass.api.OrderEntity>>, 
                 response: Response<List<com.example.base44.dataClass.api.OrderEntity>>
             ) {
                 if (response.isSuccessful && response.body() != null) {
                     val allOrders = response.body()!!
                     
                     // Client-side filtering (Check both ID and createdBy/Email)
                     val userOrders = allOrders.filter { 
                         (it.userId == userId) || 
                         (!it.createdBy.isNullOrEmpty() && !userEmail.isNullOrEmpty() && it.createdBy == userEmail)
                     }
                     
                     val gson = com.google.gson.Gson()
                     val type = object : com.google.gson.reflect.TypeToken<List<com.example.base44.dataClass.add_to_cart_item>>() {}.type
                     
                     val mappedOrders = mutableListOf<OrderItem>()
                     
                     userOrders.forEach { entity ->
                         try {
                              val cartItems: List<com.example.base44.dataClass.add_to_cart_item> = gson.fromJson(entity.itemsJson, type) ?: emptyList()
                              val firstItem = cartItems.firstOrNull()
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
                              mappedOrders.add(item)
                         } catch (e: Exception) {
                             android.util.Log.e("WALLET_API", "Error parsing order: ${e.message}")
                         }
                     }
                     
                     orders = mappedOrders
                     filterOrders()
                     
                 } else {
                     android.util.Log.e("WALLET_API", "Failed to load orders: ${response.code()}")
                 }
             }
             
             override fun onFailure(call: Call<List<com.example.base44.dataClass.api.OrderEntity>>, t: Throwable) {
                 android.util.Log.e("WALLET_API", "Network error loading orders", t)
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
                .findViewById<BottomNavigationView>(R.id.bottomNavigation)
                .selectedItemId = R.id.nav_home
        }
    }
}
