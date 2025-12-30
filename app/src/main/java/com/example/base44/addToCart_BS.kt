package com.example.base44

import android.R.attr.dropDownHeight
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MyBottomSheet(private val itemName: String) : BottomSheetDialogFragment() {

    private lateinit var tvItemHeading: TextView
    private lateinit var etPasteSlip: EditText
    private lateinit var btnProcess: Button
    private lateinit var btnAddRow: Button
    private lateinit var dayCheckboxes: List<CheckBox>
    private lateinit var spinnerValues: List<String>

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
    }


    private fun initViews(view: View) {
        tvItemHeading = view.findViewById(R.id.tvItemHeading)
        etPasteSlip = view.findViewById(R.id.etPasteSlip)
        btnProcess = view.findViewById(R.id.btnProcess)
        btnAddRow = view.findViewById(R.id.btnAddRow)
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
        spinner.layoutParams = TableRow.LayoutParams(0, 35.dpToPx(), 1f).apply { marginStart = 4.dpToPx() }
            spinner.setBackgroundResource(R.drawable.bg_date_field)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerValues)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set drop-down width & height safely
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

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
