package com.example.base44

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginStart
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MyBottomSheet(private val itemName: String) : BottomSheetDialogFragment() {

    private lateinit var tvItemHeading: TextView
    private lateinit var etPasteSlip: EditText
    private lateinit var btnProcess: Button
    private lateinit var btnAddRow: Button
    private lateinit var dayCheckboxes: List<CheckBox>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout
        val view =  inflater.inflate(R.layout.fragment_add_to_cart__b_s, container, false)

        val spinnerValues = mutableListOf<String>()
        var value = 1.0
        while (value <= 10.0) {
            spinnerValues.add(String.format("%.1f", value)) // 1.0, 1.5, 2.0 ...
            value += 0.5
        }

        tvItemHeading = view.findViewById(R.id.tvItemHeading)
        etPasteSlip = view.findViewById(R.id.etPasteSlip)
        btnProcess = view.findViewById(R.id.btnProcess)
        btnAddRow = view.findViewById(R.id.btnAddRow)

        tvItemHeading.text = "Add $itemName to Cart"


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialRows = 3
        repeat(initialRows) {
            addNewRowToTable()
        }

        setupAddRowButton()
    }

    private fun addNewRowToTable (){
        val tableLayout = view?.findViewById<TableLayout>(R.id.tableLayout) ?: return
        val nestedScroll = view?.findViewById<NestedScrollView>(R.id.nestedScrollView)
        val tableRow = TableRow(requireContext())
        tableRow.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        tableRow.setPadding(0 , 0, 0 , 8.dpToPx())

        val editText = EditText(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(0, 35.dpToPx(), 1f)
            hint = "No"
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_date_field)
            textSize = 12f
            setPadding(4.dpToPx(), 0 , 4.dpToPx(), 0)
        }
        tableRow.addView(editText)

        val spinner = Spinner(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(0, 35.dpToPx(), 1f).apply {
                marginStart = 4.dpToPx()
            }
            setBackgroundResource(R.drawable.bg_date_field)
        }
        tableRow.addView(spinner)

        repeat(7) {
            val checkBox = CheckBox(requireContext()).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )

                setPadding(4.dpToPx(),4.dpToPx(),4.dpToPx(),4.dpToPx())

            }
            tableRow.addView(checkBox)
        }

        tableLayout.addView(tableRow)

        nestedScroll?.post {
            nestedScroll.scrollTo(0, tableLayout.bottom)
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun setupAddRowButton() {
        val btnAddRow = requireView().findViewById<Button>(R.id.btnAddRow)
        btnAddRow.setOnClickListener {
            addNewRowToTable()
        }
    }



}
