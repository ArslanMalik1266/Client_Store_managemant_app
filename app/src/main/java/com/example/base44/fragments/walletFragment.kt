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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

    private val uid = FirebaseAuth.getInstance().uid
    private val db = FirebaseFirestore.getInstance()

    private var creditLimit = 5000.0
    private var currentAvailableBalance = 0.0
    private var orders = listOf<OrderItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_wallet, container, false)

        // Initialize views
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


    private fun fetchBalance() {
        if (uid == null) return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                currentAvailableBalance = doc.getDouble("balance") ?: creditLimit
                updateBalanceUI()
            }
            .addOnFailureListener {
                currentAvailableBalance = creditLimit
                updateBalanceUI()
            }
    }

    private fun setupChips() {
        val chips = listOf(chipToday, chipYesterday, chipThisWeek, chipAll)
        chips.forEach { chip ->
            chip.setOnClickListener { filterOrders() }
        }
    }

    private fun updateCreditDueWeek() {
        val creditUsedThisWeek = orders
            .filter { it.isThisWeek() }  // only this week's orders
            .sumOf { order ->
                order.totalAmount.toDoubleOrNull() ?: 0.0
            }

        creditDueWeekText.text = "RM %.2f".format(creditUsedThisWeek)
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
    }

    private fun updateStats(filteredOrders: List<OrderItem>) {
        val totalSales = filteredOrders.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }
        tvPeriodSaleAmount.text = "RM %.2f".format(totalSales)

        // Commission example: 25%
        val commission = totalSales * 0.25
        tvPeriodCommissionAmount.text = "RM %.2f".format(commission)

        val weekOrders = orders.filter { it.isThisWeek() }
        val weekCommission = weekOrders.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 } * 0.25
        weekCommissionText.text = "RM %.2f".format(weekCommission)
        val nextMonday = getNextMonday()
        view?.findViewById<TextView>(R.id.comissionText)?.text = "Pay on $nextMonday"
    }

    private fun getNextMonday(): String {
        val calendar = Calendar.getInstance()
        // Set to next Monday
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val daysUntilMonday = (Calendar.MONDAY - today + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, if (daysUntilMonday == 0) 7 else daysUntilMonday)

        val sdf = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    private fun updateBalanceUI() {
        walletBalance.text = "Credit Limit: RM %.2f".format(creditLimit)
        availableBalance.text = "RM %.2f".format(currentAvailableBalance)

        val usedAmount = creditLimit - currentAvailableBalance
        val percentUsed = ((usedAmount / creditLimit) * 100).coerceIn(0.0, 100.0)
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
        if (uid == null) return

        db.collection("users").document(uid)
            .collection("orders")
            .get()
            .addOnSuccessListener { result ->
                orders = result.map { doc ->
                    val rowsList = (doc.get("rows") as? List<Map<String, Any>>)?.map { rowMap ->
                        CartRow(
                            number = rowMap["number"] as? String ?: "",
                            amount = rowMap["amount"] as? String ?: "",
                            selectedCategories = rowMap["selectedCategories"] as? List<String> ?: emptyList(),
                            qty = (rowMap["qty"] as? Long)?.toInt() ?: 1
                        )
                    } ?: emptyList()

                    OrderItem(
                        invoiceNumber = doc.getString("invoiceNumber") ?: "",
                        dateAdded = doc.getString("dateAdded") ?: "",
                        totalAmount = doc.getString("totalAmount") ?: "0",
                        status = doc.getString("status") ?: "",
                        rows = rowsList
                    )
                }.reversed()

                filterOrders()
                updateCreditDueWeek()

            }
            .addOnFailureListener {
                adapter.updateData(emptyList())
            }
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
