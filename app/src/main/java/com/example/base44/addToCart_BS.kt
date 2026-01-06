package com.example.base44

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.adaptor.ProductAdapter
import com.example.base44.dataClass.CartManager
import com.example.base44.dataClass.CartRow
import com.example.base44.dataClass.Product
import com.example.base44.dataClass.add_to_cart_item
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class MyBottomSheet() : BottomSheetDialogFragment() {

    private var singleProduct: Product? = null
    private var multipleProducts: List<Product>? = null

    constructor(product: Product) : this() {
        singleProduct = product
    }

    constructor(products: List<Product>) : this() {
        multipleProducts = products
    }

    private lateinit var tvItemHeading: TextView
    private lateinit var etPasteSlip: EditText
    private lateinit var btnAddRow: Button
    private lateinit var btnAddToCart: Button
    private lateinit var tvTotalAmount: TextView
    private lateinit var spinnerValues: List<String>

    private val categoryMap = listOf("B", "X", "A", "IB", "BX", "BXA", "BXS")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_to_cart__b_s, container, false)
        initViews(view)
        setupSpinnerValues()
        setupInitialRows(3)
        setupAddRowButton()
        setupPasteSlipListener()

        tvItemHeading.text = when {
            singleProduct != null -> "Add ${singleProduct!!.title} to Cart"
            multipleProducts != null -> {
                val names = multipleProducts!!.joinToString(", ") { it.title }
                "Add $names to Cart"
            }
            else -> "Add Item to Cart"
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRaceDaysListeners()
        btnAddToCart.setOnClickListener {
            if (!isCartDataValid()) {
                Toast.makeText(
                    requireContext(),
                    "Please fill at least 1 row with number, amount, and at least one category.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            singleProduct?.let {
                val cartItem = collectData(it.title, it.imageRes, it.code)
                cartItem.tempInvoice = generateTempInvoice()
                CartManager.addItem(cartItem)
                it.isAddedToCart = false
            }

            multipleProducts?.forEach { product ->
                val cartItem = collectData(product.title, product.imageRes, product.code)
                cartItem.tempInvoice = generateTempInvoice()
                CartManager.addItem(cartItem)
            }
            multipleProducts?.forEach { it.isAddedToCart = false }

            (activity as? MainActivity)?.let { main ->
                (main.findViewById<RecyclerView>(R.id.rvProducts)?.adapter as? ProductAdapter)
                    ?.notifyDataSetChanged()
                main.updateSelectedItems(emptyList())
            }

            dismiss()
        }
    }

    private fun initViews(view: View) {
        tvItemHeading = view.findViewById(R.id.tvItemHeading)
        etPasteSlip = view.findViewById(R.id.etPasteSlip)
        btnAddRow = view.findViewById(R.id.btnAddRow)
        btnAddToCart = view.findViewById(R.id.btnAddToCart)
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount_a)
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

    private fun setupAddRowButton() {
        btnAddRow.setOnClickListener { addNewRowToTable() }
    }
    private fun setupRaceDaysListeners() {
        view?.findViewById<LinearLayout>(R.id.raceDaysLayout)?.let { layout ->
            for (i in 0 until layout.childCount) {
                val cb = layout.getChildAt(i) as? CheckBox ?: continue
                cb.setOnCheckedChangeListener { _, _ -> updateTotalAmount() }
            }
        }
    }


    private fun setupPasteSlipListener() {
        etPasteSlip.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateTotalAmount() }
        })
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

        val numberEditText = createEditText()
        numberEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateTotalAmount() }
        })

        val spinner = createSpinner()
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateTotalAmount()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        tableRow.addView(numberEditText)
        tableRow.addView(spinner)

        repeat(7) {
            val cb = createCheckBox()
            cb.setOnCheckedChangeListener { _, _ -> updateTotalAmount() }
            tableRow.addView(cb)
        }

        tableLayout.addView(tableRow)
        nestedScroll?.post { nestedScroll.scrollTo(0, tableLayout.bottom) }
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
        return spinner
    }

    private fun createCheckBox(): CheckBox = CheckBox(requireContext()).apply {
        layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
    }


    private fun getTodayName(): String {
        val sdf = SimpleDateFormat("EEE", Locale.ENGLISH)
        val dayShort = sdf.format(Date())
        return when (dayShort) {
            "Mon" -> "Mon"
            "Tue" -> "Tue"
            "Wed" -> "Wed"
            "Thu" -> "Thu"
            "Fri" -> "Fri"
            "Sat" -> "Sat"
            "Sun" -> "Sun"
            else -> "Mon" // fallback
        }
    }


    private fun isCartDataValid(): Boolean {
        val tableLayout = view?.findViewById<TableLayout>(R.id.tableLayout) ?: return false
        for (i in 1 until tableLayout.childCount) {
            val row = tableLayout.getChildAt(i) as? TableRow ?: continue
            val number = (row.getChildAt(0) as EditText).text.toString().trim()
            var hasChecked = false
            for (j in 2 until row.childCount) {
                if ((row.getChildAt(j) as? CheckBox)?.isChecked == true) hasChecked = true
            }
            if (number.isNotBlank() && hasChecked) return true
        }
        return false
    }
    private fun updateTotalAmount() {
        val tableLayout = view?.findViewById<TableLayout>(R.id.tableLayout) ?: return
        val raceDaysLayout = view?.findViewById<LinearLayout>(R.id.raceDaysLayout)
        val selectedRaceDays = mutableListOf<String>()

        // Collect selected race days
        for (i in 0 until (raceDaysLayout?.childCount ?: 0)) {
            val cb = raceDaysLayout?.getChildAt(i) as? CheckBox
            cb?.let { if (it.isChecked) selectedRaceDays.add(it.text.toString()) }
        }

        // If no race day selected, use today
        if (selectedRaceDays.isEmpty()) selectedRaceDays.add(getTodayName())

        var total = 0.0

        for (i in 1 until tableLayout.childCount) { // skip header
            val row = tableLayout.getChildAt(i) as? TableRow ?: continue
            val number = (row.getChildAt(0) as EditText).text.toString().trim()
            val amount = (row.getChildAt(1) as Spinner).selectedItem.toString().toDouble()
            if (number.isBlank()) continue

            var qtyForRow = 0

            for (j in 2 until row.childCount) {
                val cb = row.getChildAt(j) as? CheckBox ?: continue
                if (!cb.isChecked) continue
                val type = categoryMap.getOrNull(j - 2) ?: continue

                // Add qty properly
                qtyForRow += if (type == "BX" || type == "BXA" || type == "BXS") {
                    calculatePermutations(number)
                } else {
                    1
                }
            }

            if (qtyForRow > 0) {
                total += amount * qtyForRow * selectedRaceDays.size
            }
        }

        tvTotalAmount.text = "RM %.2f".format(total)
    }



    private fun collectData(name: String, image: Int, code: String): add_to_cart_item {
        val tableLayout = view?.findViewById<TableLayout>(R.id.tableLayout)
        val raceDaysLayout = view?.findViewById<LinearLayout>(R.id.raceDaysLayout)

        val selectedRaceDays = mutableListOf<String>()
        for (i in 0 until (raceDaysLayout?.childCount ?: 0)) {
            val cb = raceDaysLayout?.getChildAt(i) as? CheckBox
            cb?.let { if (it.isChecked) selectedRaceDays.add(it.text.toString()) }
        }
        if (selectedRaceDays.isEmpty()) selectedRaceDays.add(getTodayName())

        val rowsList = mutableListOf<CartRow>()

        for (i in 1 until (tableLayout?.childCount ?: 0)) {
            val row = tableLayout?.getChildAt(i) as? TableRow ?: continue
            val number = (row.getChildAt(0) as EditText).text.toString().trim()
            val amount = (row.getChildAt(1) as Spinner).selectedItem.toString().toDouble()
            if (number.isBlank()) continue

            val categories = mutableListOf<String>()
            var qtyForRow = 0

            for (j in 2 until row.childCount) {
                val cb = row.getChildAt(j) as? CheckBox ?: continue
                if (!cb.isChecked) continue
                val type = categoryMap.getOrNull(j - 2) ?: continue
                categories.add(type)

                qtyForRow += if (type == "BX" || type == "BXA" || type == "BXS") {
                    calculatePermutations(number)
                } else {
                    1
                }
            }

            if (categories.isNotEmpty() && qtyForRow > 0) {
                rowsList.add(CartRow(number, amount.toString(), categories, qtyForRow))
            }
        }

        // TOTAL = sum of each row qty * row amount * number of selected race days
        val totalText = tvTotalAmount.text.toString().replace("RM ", "").toDoubleOrNull() ?: 0.0
        val total = totalText

        val bettingSlipText = view?.findViewById<EditText>(R.id.etPasteSlip)?.text?.toString()?.takeIf { it.isNotBlank() }

        return add_to_cart_item(
            productName = name,
            bettingSlip = bettingSlipText,
            raceDays = selectedRaceDays,
            rows = rowsList,
            imageRes = image,
            productCode = code,
            totalAmount = total
        )
    }





    private fun calculatePermutations(number: String): Int {
        val freq = mutableMapOf<Char, Int>()
        number.forEach { c -> freq[c] = freq.getOrDefault(c, 0) + 1 }
        var total = factorial(number.length)
        freq.values.forEach { total /= factorial(it) }
        return total
    }

    private fun factorial(n: Int): Int {
        var result = 1
        for (i in 2..n) result *= i
        return result
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun generateTempInvoice(): String {
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        val date = sdf.format(Date())
        return "$date/TNaN"
    }
}
