package com.example.base44

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TableRow
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MyBottomSheet(private val itemName: String) : BottomSheetDialogFragment() {

    private lateinit var tvItemHeading: TextView
    private lateinit var etPasteSlip: EditText
    private lateinit var btnProcess: Button
    private lateinit var dayCheckboxes: List<CheckBox>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout
        val view =  inflater.inflate(R.layout.fragment_add_to_cart__b_s, container, false)


        tvItemHeading = view.findViewById(R.id.tvItemHeading)
        etPasteSlip = view.findViewById(R.id.etPasteSlip)
        btnProcess = view.findViewById(R.id.btnProcess)

        tvItemHeading.text = "Add $itemName to Cart"



        return view
    }

}
