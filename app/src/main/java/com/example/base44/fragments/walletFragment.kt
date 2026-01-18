package com.example.base44.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.MainActivity
import com.example.base44.R
import com.example.base44.adaptor.SimpleOrdersAdapter
import com.example.base44.dataClass.SimpleOrderItem
import com.example.base44.repository.WalletRepository
import com.example.base44.viewmodels.WalletViewModel
import com.example.base44.viewmodels.WalletViewModelFactory
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

    private lateinit var chipToday: Chip
    private lateinit var chipYesterday: Chip
    private lateinit var chipThisWeek: Chip
    private lateinit var chipAll: Chip
    private lateinit var chipGroup: ChipGroup

    private var creditLimit = 5000.0
    private var currentAvailableBalance = 0.0
    private var commissionRate = 0

    private lateinit var viewModel: WalletViewModel
    private val REQUEST_CODE_PICK_IMAGE = 1001
    private var selectedImageUri: Uri? = null
    private var dialogImagePreview: ImageView? = null
    private var layoutUploadPlaceholder: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_wallet, container, false)

        // ---------------- UI INIT ----------------
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

        btnUploadProof.setOnClickListener { showUploadProofDialog() }

        chipToday = view.findViewById(R.id.chipToday)
        chipYesterday = view.findViewById(R.id.chipYesterday)
        chipThisWeek = view.findViewById(R.id.chipThisWeek)
        chipAll = view.findViewById(R.id.chipAll)
        chipGroup = view.findViewById(R.id.chipGroupFilter)

        setupToolbar(view)
        setupRecyclerView(view)
        setupChips()

        chipAll.isChecked = true

        // ---------------- ViewModel Init ----------------
        viewModel = ViewModelProvider(
            this,
            WalletViewModelFactory(WalletRepository())
        )[WalletViewModel::class.java]

        viewModel.userData.observe(viewLifecycleOwner) { user ->
            commissionRate = user.commissionRate ?: 0
            commissionRateText.text = "$commissionRate%"
            creditLimit = user.creditLimit ?: 5000.0
            currentAvailableBalance = user.currentBalance ?: 0.0
            updateBalanceUI()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            view.findViewById<ProgressBar>(R.id.loadingProgressBar)?.visibility =
                if (isLoading) View.VISIBLE else View.GONE
            view.findViewById<androidx.core.widget.NestedScrollView>(R.id.contentScrollView)?.visibility =
                if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.simpleOrders.observe(viewLifecycleOwner) { simpleOrders ->
            adapter.updateData(simpleOrders)
        }

        viewModel.periodSales.observe(viewLifecycleOwner) { sales ->
            tvPeriodSaleAmount.text = "RM %.2f".format(sales)
        }
        viewModel.periodCommission.observe(viewLifecycleOwner) { commission ->
            tvPeriodCommissionAmount.text = "RM %.2f".format(commission)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        hideToolbarAndDrawer()

        val session = com.example.base44.adaptor.utils.SessionManager(requireContext())
        val userId = session.getUserId()
        userId?.let { viewModel.fetchUserProfile(it) }
        viewModel.fetchOrders()
    }

    private fun setupChips() {
        chipToday.setOnClickListener { viewModel.filterOrders(WalletViewModel.OrderFilter.TODAY) }
        chipYesterday.setOnClickListener { viewModel.filterOrders(WalletViewModel.OrderFilter.YESTERDAY) }
        chipThisWeek.setOnClickListener { viewModel.filterOrders(WalletViewModel.OrderFilter.THIS_WEEK) }
        chipAll.setOnClickListener { viewModel.filterOrders(WalletViewModel.OrderFilter.ALL) }
    }

    private fun updateBalanceUI() {
        walletBalance.text = "Credit Limit: RM %.2f".format(creditLimit)
        availableBalance.text = "RM %.2f".format(currentAvailableBalance)

        val used = creditLimit - currentAvailableBalance
        val percent = ((used / creditLimit) * 100).coerceIn(0.0, 100.0)
        usedPercentText.text = "%.1f%% used".format(percent)
        progressBar.progress = percent.toInt()

        (activity as? MainActivity)?.let { main ->
            main.userAvailableBalance = currentAvailableBalance
            viewModel.userData.value?.let { user -> main.currentUser = user }
        }

        updateStats()
    }

    private fun updateStats() {
        val user = viewModel.userData.value ?: return
        weekCommissionText.text = "RM %.2f".format(user.weeklyCommission ?: 0.0)
        creditDueWeekText.text = "RM %.2f".format(user.outstandingDebt ?: 0.0)
        view?.findViewById<TextView>(R.id.comissionText)?.text = "Pay on ${getNextMonday()}"
        lastCommissionDate.text = user.lastCommissionDate?.let { formatDate(it) } ?: "N/A"
        lastCommissionAmount.text = "RM %.2f".format(user.lastCommissionPaid ?: 0.0)
        commissionRateText.text = "${user.commissionRate ?: 0}%"
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

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SimpleOrdersAdapter()
        recyclerView.adapter = adapter
    }

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

    private fun getNextMonday(): String {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val diff = (Calendar.MONDAY - today + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, if (diff == 0) 7 else diff)
        return SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()).format(calendar.time)
    }

    private val pickImageLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = it
                dialogImagePreview?.setImageURI(it)
                dialogImagePreview?.visibility = View.VISIBLE
                layoutUploadPlaceholder?.visibility = View.GONE
            }
        }

    private fun showUploadProofDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_upload_proof, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvCreditDueAmount: TextView = dialogView.findViewById(R.id.tvCreditDueAmount)
        val cardUploadArea: com.google.android.material.card.MaterialCardView =
            dialogView.findViewById(R.id.cardUploadArea)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)
        val btnSubmitProof: Button = dialogView.findViewById(R.id.btnSubmitProof)

        dialogImagePreview = dialogView.findViewById(R.id.ivSelectedProof)
        layoutUploadPlaceholder = dialogView.findViewById(R.id.layoutUploadPlaceholder)

        tvCreditDueAmount.text = "RM %.2f".format(viewModel.userData.value?.outstandingDebt ?: 0.0)
        selectedImageUri = null
        dialogImagePreview?.visibility = View.GONE
        layoutUploadPlaceholder?.visibility = View.VISIBLE

        cardUploadArea.setOnClickListener { pickImageLauncher.launch("image/*") }
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSubmitProof.setOnClickListener {
            selectedImageUri?.let { uri ->
                val file = uriToFile(uri, requireContext())
                val user = viewModel.userData.value ?: return@setOnClickListener
                viewModel.uploadPaymentProof(
                    userEmail = user.email ?: "",
                    amount = user.outstandingDebt ?: 0.0,
                    payment_proof_url = file,
                    notes = "Payment proof submitted"
                )
                viewModel.uploadResult.observe(viewLifecycleOwner) { msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            } ?: run {
                Toast.makeText(requireContext(), "Please upload a screenshot first", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
        return file
    }
}
