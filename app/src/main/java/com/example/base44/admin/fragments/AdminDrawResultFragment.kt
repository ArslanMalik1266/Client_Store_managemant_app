package com.example.base44.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.base44.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import java.util.TimeZone

class AdminDrawResultFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_draw_result, container, false)

        val btnAdd = view.findViewById<MaterialButton>(R.id.btnAddDrawResult)
        btnAdd.setOnClickListener {
            showAddDrawResultDialog()
        }

        return view
    }

    private fun showAddDrawResultDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_draw_result, null)
        
        val spinnerProducts = dialogView.findViewById<Spinner>(R.id.spinnerProducts)
        val tvSelectedDate = dialogView.findViewById<TextView>(R.id.tvSelectedDate)
        val cardDatePicker = dialogView.findViewById<View>(R.id.cardDatePicker)
        val cardUploadImage = dialogView.findViewById<View>(R.id.cardUploadImage)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSubmit = dialogView.findViewById<MaterialButton>(R.id.btnSubmitResult)

        val etFirst = dialogView.findViewById<android.widget.EditText>(R.id.etFirstPrize)
        val etSecond = dialogView.findViewById<android.widget.EditText>(R.id.etSecondPrize)
        val etThird = dialogView.findViewById<android.widget.EditText>(R.id.etThirdPrize)
        val etSpecial = dialogView.findViewById<android.widget.EditText>(R.id.etSpecialPrizes)
        val etConsolation = dialogView.findViewById<android.widget.EditText>(R.id.etConsolationPrizes)
        
        // Setup Product Spinner
        val products = arrayOf("Kuda", "Todo", "Magnum", "SG Pools", "Sabah", "Dragon", "Lotto", "9Lotto", "Special Draw")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, products)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProducts.adapter = adapter
        
        // Setup Date Picker
        cardDatePicker.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Draw Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
                
            datePicker.addOnPositiveButtonClickListener { selection ->
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = selection
                val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                tvSelectedDate.text = format.format(calendar.time)
            }
            datePicker.show(childFragmentManager, "DATE_PICKER")
        }
        
        // Setup Image Upload (Placeholder)
        cardUploadImage.setOnClickListener {
            Toast.makeText(context, "Image Upload logic will be implemented soon", Toast.LENGTH_SHORT).show()
        }
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .show()
            
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSubmit.setOnClickListener {
            val selectedProduct = spinnerProducts.selectedItem.toString()
            val selectedDate = tvSelectedDate.text.toString()
            val first = etFirst.text.toString()
            val second = etSecond.text.toString()
            val third = etThird.text.toString()
            val special = etSpecial.text.toString()
            val consolation = etConsolation.text.toString()
            
            if (selectedDate == "Select Date") {
                Toast.makeText(context, "Please select a draw date", Toast.LENGTH_SHORT).show()
            } else if (first.isEmpty() || second.isEmpty() || third.isEmpty()) {
                Toast.makeText(context, "Please fill all main prize numbers", Toast.LENGTH_SHORT).show()
            } else {
                // Future point for Firestore integration
                Toast.makeText(context, "Result saved for $selectedProduct", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }
}
