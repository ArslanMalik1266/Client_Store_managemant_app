package com.example.base44.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.MainActivity
import com.example.base44.R
import com.example.base44.adaptor.SimpleOrdersAdapter
import com.example.base44.dataClass.OrderItem
import com.example.base44.dataClass.SimpleOrderItem
import com.example.base44.dataClass.isThisWeek
import com.example.base44.dataClass.isToday
import com.example.base44.dataClass.isYesterday
import com.example.base44.repository.WalletRepository
import com.example.base44.viewmodel.WalletViewModel
import com.example.base44.viewmodel.WalletViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class walletFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpleOrdersAdapter
    private lateinit var walletBalance: TextView
    private lateinit var availableBalance: TextView
    private lateinit var usedPercentText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var commissionRateText: TextView

    private lateinit var tvPeriodSaleAmount: TextView
    private lateinit var tvPeriodCommissionAmount: TextView
    private lateinit var weekCommissionText: TextView
    private lateinit var creditDueWeekText: TextView
    private lateinit var lastCommissionAmount: TextView
    private lateinit var lastCommissionDate: TextView
    private lateinit var btnUploadProof: Button
    private val REQUEST_CODE_PICK_IMAGE = 1001
    private var selectedImageUri: android.net.Uri? = null
    private var dialogImagePreview: android.widget.ImageView? = null
    private var layoutUploadPlaceholder: android.widget.LinearLayout? = null

    private lateinit var chipToday: Chip
    private lateinit var chipYesterday: Chip
    private lateinit var chipThisWeek: Chip
    private lateinit var chipAll: Chip
    private lateinit var chipGroup: ChipGroup

    private var creditLimit = 5000.0
    private var currentAvailableBalance = 0.0
    private var commissionRate = 0

    private lateinit var viewModel: WalletViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_wallet, container, false)

        // -------------------
        // UI INIT
        // -------------------
        walletBalance = view.findViewById(R.id.walletBalance)
        availableBalance = view.findViewById(R.id.availableBalance)
        usedPercentText = view.findViewById(R.id.usedPercentText)
        progressBar = view.findViewById(R.id.progressBar)
        tvPeriodSaleAmount = view.findViewById(R.id.tvPeriodSaleAmount)
        tvPeriodCommissionAmount = view.findViewById(R.id.tvPeriodCommissionAmount)
        weekCommissionText = view.findViewById(R.id.weekCommission_id)
        creditDueWeekText = view.findViewById(R.id.creditDueWeek_text)
        commissionRateText = view.findViewById(R.id.comission_rate)
        lastCommissionAmount = view.findViewById(R.id.lastComissionPaid_Value)
        lastCommissionDate = view.findViewById(R.id.payment_date_lastComission)
        btnUploadProof = view.findViewById(R.id.btnUploadProof)

        btnUploadProof.setOnClickListener {
            showUploadProofDialog()
        }



        chipToday = view.findViewById(R.id.chipToday)
        chipYesterday = view.findViewById(R.id.chipYesterday)
        chipThisWeek = view.findViewById(R.id.chipThisWeek)
        chipAll = view.findViewById(R.id.chipAll)
        chipGroup = view.findViewById(R.id.chipGroupFilter)

        setupToolbar(view)
        setupRecyclerView(view)
        setupChips()

        chipAll.isChecked = true

        // -------------------
        // ViewModel Init
        // -------------------
        viewModel = ViewModelProvider(
            this,
            WalletViewModelFactory(WalletRepository())
        )[WalletViewModel::class.java]

        // Observe user profile
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            commissionRate = user.commissionRate ?: 0
            commissionRateText.text = "$commissionRate%"
            creditLimit = user.creditLimit ?: 5000.0
            currentAvailableBalance = user.currentBalance ?: 0.0
            updateBalanceUI()
        }

        // Observe orders for RecyclerView & Stats
        viewModel.simpleOrders.observe(viewLifecycleOwner) { simpleOrders ->
            adapter.updateData(simpleOrders)
        }

        return view
    }


    override fun onResume() {
        super.onResume()
        hideToolbarAndDrawer()

        // Load data
        val session = com.example.base44.adaptor.utils.SessionManager(requireContext())
        val userId = session.getUserId()
        userId?.let { viewModel.fetchUserProfile(it) }
        viewModel.fetchOrders()
    }

    fun refreshOrdersAndCredit() {
        viewModel.fetchOrders()
    }


    // -------------------
    // Chips Filtering
    // -------------------
    private fun setupChips() {
        chipToday.setOnClickListener { viewModel.filterOrders(WalletViewModel.OrderFilter.TODAY) }
        chipYesterday.setOnClickListener { viewModel.filterOrders(WalletViewModel.OrderFilter.YESTERDAY) }
        chipThisWeek.setOnClickListener { viewModel.filterOrders(WalletViewModel.OrderFilter.THIS_WEEK) }
        chipAll.setOnClickListener { viewModel.filterOrders(WalletViewModel.OrderFilter.ALL) }
    }

    // -------------------
    // Stats & Balance UI
    // -------------------
    private fun updateBalanceUI() {
        walletBalance.text = "Credit Limit: RM %.2f".format(creditLimit)
        availableBalance.text = "RM %.2f".format(currentAvailableBalance)

        val used = creditLimit - currentAvailableBalance
        val percent = ((used / creditLimit) * 100).coerceIn(0.0, 100.0)
        usedPercentText.text = "%.1f%% used".format(percent)
        progressBar.progress = percent.toInt()

        // Update MainActivity variable
        (activity as? MainActivity)?.userAvailableBalance = currentAvailableBalance

        updateStats()
    }

    private fun updateStats() {
        val orders = viewModel.orders.value ?: emptyList()
        val user = viewModel.userData.value
        val outstandingDebt = user.outstandingDebt ?: 0.0
        val commissionRate = user?.commissionRate?.toDouble() ?: 0.0
        val totalSales = orders.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }
        tvPeriodSaleAmount.text = "RM %.2f".format(totalSales)

        val commission = totalSales * (commissionRate / 100)
        tvPeriodCommissionAmount.text = "RM %.2f".format(commission)

        val weekOrders = orders.filter { it.isThisWeek() }
        val weekTotal = weekOrders.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }
        val weekCommission = weekTotal * (commissionRate / 100)
        weekCommissionText.text = "RM %.2f".format(weekCommission)
        creditDueWeekText.text = "RM %.2f".format(outstandingDebt)
        weekCommissionText.text = "RM %.2f".format(user.weeklyCommission ?: 0.0)

        val nextMonday = getNextMonday()
        view?.findViewById<TextView>(R.id.comissionText)?.text = "Pay on $nextMonday"

        val lastDate = user.lastCommissionDate
        lastCommissionDate.text = if (!lastDate.isNullOrEmpty()) {
            formatDate(lastDate)
        } else {
            "N/A"
        }

        lastCommissionAmount.text = "RM %.2f".format(user.lastCommissionPaid)
    }


    private fun formatDate(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val parsedDate = inputFormat.parse(date)
            val outputFormat = SimpleDateFormat("dd MMM yyyy , hh:mma", Locale.getDefault())
            outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            date
        }
    }

    // -------------------
    // RecyclerView
    // -------------------
    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SimpleOrdersAdapter(emptyList())
        recyclerView.adapter = adapter
    }

    // -------------------
    // Toolbar & Drawer
    // -------------------
    private fun setupToolbar(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.topAppBar)
        toolbar.setNavigationIcon(R.drawable.back_arrow)
        toolbar.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
                .selectedItemId = R.id.nav_home
        }
    }

    private fun hideToolbarAndDrawer() {
        val act = activity as? MainActivity ?: return
        act.toolbar.visibility = View.GONE
        act.enableDrawer(false)
    }

    // -------------------
    // Utils
    // -------------------
    private fun getNextMonday(): String {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val diff = (Calendar.MONDAY - today + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, if (diff == 0) 7 else diff)
        return SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()).format(calendar.time)
    }
    private val pickImageLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            dialogImagePreview?.setImageURI(uri)
            dialogImagePreview?.visibility = View.VISIBLE
            layoutUploadPlaceholder?.visibility = View.GONE
        }
    }

    private fun showUploadProofDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_upload_proof, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        // UI references
        val tvCreditDueAmount: TextView = dialogView.findViewById(R.id.tvCreditDueAmount)
        val cardUploadArea: com.google.android.material.card.MaterialCardView = dialogView.findViewById(R.id.cardUploadArea)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)
        val btnSubmitProof: Button = dialogView.findViewById(R.id.btnSubmitProof)

        dialogImagePreview = dialogView.findViewById(R.id.ivSelectedProof)
        layoutUploadPlaceholder = dialogView.findViewById(R.id.layoutUploadPlaceholder)


        val user = viewModel.userData.value
        val outstandingDebt = user?.outstandingDebt ?: 0.0
        tvCreditDueAmount.text = "RM %.2f".format(outstandingDebt)

        selectedImageUri = null
        dialogImagePreview?.visibility = View.GONE
        layoutUploadPlaceholder?.visibility = View.VISIBLE

        // Listeners
        cardUploadArea.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSubmitProof.setOnClickListener {
            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Please upload a screenshot first", Toast.LENGTH_SHORT).show()
            } else {
                // Convert URI to File
                val file = uriToFile(selectedImageUri!!, requireContext())
                val user = viewModel.userData.value
                if (user != null) {
                    viewModel.uploadPaymentProof(
                        userEmail = user.email ?: "",
                        amount = user.outstandingDebt ?: 0.0,
                        payment_proof_url = file,
                        notes = "Payment proof submitted"
                    )

                    // Observe upload result
                    viewModel.uploadResult.observe(viewLifecycleOwner) { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }

                dialog.dismiss()
            }
        }


        dialog.show()
    }
    fun uriToFile(uri: Uri, context: Context): File {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
        return file
    }
}

