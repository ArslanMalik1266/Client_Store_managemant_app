package com.example.base44

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import com.example.base44.dataClass.CartManager
import com.example.base44.dataClass.CartRow
import com.example.base44.dataClass.add_to_cart_item
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MyBottomSheet(private val itemName: String) : BottomSheetDialogFragment() {

    private lateinit var tvItemHeading: TextView
    private lateinit var etPasteSlip: EditText
    private lateinit var btnProcess: Button
    private lateinit var btnAddRow: Button
    private lateinit var spinnerValues: List<String>
    private lateinit var btnAddToCart: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_to_cart__b_s, container, false)
        initViews(view)
        setupSpinnerValues()
        tvItemHeading.text = "Add $itemName to Cart"
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInitialRows(3)
        setupAddRowButton()

        btnAddToCart.setOnClickListener {
            if (!isCartDataValid()) {
                Toast.makeText(
                    requireContext(),
                    "Please fill at least 1 row with number, amount, and at least one category.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val cartItem = collectData()
            CartManager.addItem(cartItem)
            dismiss()
        }
    }

    private fun initViews(view: View) {
        tvItemHeading = view.findViewById(R.id.tvItemHeading)
        etPasteSlip = view.findViewById(R.id.etPasteSlip)
        btnProcess = view.findViewById(R.id.btnProcess)
        btnAddRow = view.findViewById(R.id.btnAddRow)
        btnAddToCart = view.findViewById(R.id.btnAddToCart)
    }

    private fun setupSpinnerValues() {
        val tempList = mutableListOf<String>()
        var value = 1.0
        while (value <= 10.0) {
            tempList.add(String.format("%.1f", value))
            value += 0.5
        }
        spinnerValues = tempList
    }

    private fun setupInitialRows(count: Int) {
        repeat(count) { addNewRowToTable() }
    }

    private fun addNewRowToTable() {
        val tableLayout = view?.findViewById<TableLayout>(R.id.tableLayout) ?: return
        val nestedScroll = view?.findViewById<NestedScrollView>(R.id.nestedScrollView)

        val tableRow = TableRow(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 0, 0, 8.dpToPx())
        }

        tableRow.addView(createEditText())
        tableRow.addView(createSpinner())
        repeat(7) { tableRow.addView(createCheckBox()) }

        tableLayout.addView(tableRow)
        nestedScroll?.post {
            nestedScroll.scrollTo(0, tableLayout.bottom)
        }
    }

    private fun createEditText(): EditText = EditText(requireContext()).apply {
        layoutParams = TableRow.LayoutParams(0, 35.dpToPx(), 1f)
        hint = "No"
        gravity = Gravity.CENTER
        setBackgroundResource(R.drawable.bg_date_field)
        textSize = 12f
        setPadding(4.dpToPx(), 0, 4.dpToPx(), 0)
    }

    private fun createSpinner(): Spinner {
        val spinner = androidx.appcompat.widget.AppCompatSpinner(requireContext())
        spinner.layoutParams =
            TableRow.LayoutParams(0, 35.dpToPx(), 1f).apply { marginStart = 4.dpToPx() }
        spinner.setBackgroundResource(R.drawable.bg_date_field)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerValues)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.post {
            try {
                spinner.dropDownVerticalOffset = 5.dpToPx()
                spinner.dropDownHorizontalOffset = 0
                spinner.dropDownWidth = 50.dpToPx()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return spinner
    }

    private fun createCheckBox(): CheckBox = CheckBox(requireContext()).apply {
        layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
    }

    private fun setupAddRowButton() {
        btnAddRow.setOnClickListener { addNewRowToTable() }
    }

    // Validation: At least 1 valid row must exist
    private fun isCartDataValid(): Boolean {
        val tableLayout = view?.findViewById<TableLayout>(R.id.tableLayout) ?: return false
        var hasValidRow = false
        for (i in 1 until tableLayout.childCount) {
            val row = tableLayout.getChildAt(i) as? TableRow ?: continue
            val number = (row.getChildAt(0) as EditText).text.toString().trim()
            val categories = mutableListOf<String>()
            for (j in 2 until row.childCount) {
                val cb = row.getChildAt(j) as? CheckBox
                cb?.let { if (it.isChecked) categories.add(it.text.toString()) }
            }
            if (number.isNotBlank() && categories.isNotEmpty()) {
                hasValidRow = true
                break
            }
        }
        return hasValidRow
    }

    private fun collectData(): add_to_cart_item {
        val tableLayout = view?.findViewById<TableLayout>(R.id.tableLayout)
        val raceDaysLayout = view?.findViewById<LinearLayout>(R.id.raceDaysLayout)
        val selectedRaceDays = mutableListOf<String>()
        for (i in 0 until (raceDaysLayout?.childCount ?: 0)) {
            val cb = raceDaysLayout?.getChildAt(i) as? CheckBox
            cb?.let { if (it.isChecked) selectedRaceDays.add(it.text.toString()) }
        }

        val rowsList = mutableListOf<CartRow>()
        for (i in 1 until (tableLayout?.childCount ?: 0)) {
            val row = tableLayout?.getChildAt(i) as? TableRow ?: continue
            val number = (row.getChildAt(0) as EditText).text.toString().trim()
            val amount = (row.getChildAt(1) as Spinner).selectedItem.toString()
            val categories = mutableListOf<String>()
            for (j in 2 until row.childCount) {
                val cb = row.getChildAt(j) as? CheckBox
                cb?.let { if (it.isChecked) categories.add(it.text.toString()) }
            }
            if (number.isBlank() || categories.isEmpty()) continue
            rowsList.add(
                CartRow(
                    number = number,
                    amount = amount,
                    selectedCategories = categories
                )
            )
        }

        val bettingSlipText = view?.findViewById<EditText>(R.id.etPasteSlip)?.text?.toString()
            ?.takeIf { it.isNotBlank() }

        return add_to_cart_item(
            productName = itemName,
            bettingSlip = bettingSlipText,
            raceDays = selectedRaceDays,
            rows = rowsList
        )
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
