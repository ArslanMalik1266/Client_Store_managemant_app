package com.example.base44.fragments

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.MainActivity
import com.example.base44.R
import com.example.base44.adaptor.ResultAdapter
import com.example.base44.dataClass.DrawResult
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class resultFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView

    private val db = FirebaseFirestore.getInstance()
    private var allResults = listOf<DrawResult>()

    override fun onResume() {
        super.onResume()
        hideToolbarAndDrawer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_result, container, false)

        setupToolbar(view)

        recyclerView = view.findViewById(R.id.recyclerResults)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        tvEmpty = view.findViewById(R.id.empty)

        loadResults()

        return view
    }

    private fun hideToolbarAndDrawer() {
        val activity = activity as MainActivity
        activity.toolbar.visibility = View.GONE
        activity.enableDrawer(false)
    }

    private fun setupToolbar(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.topAppBar)

        // back button
        toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()

            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
            bottomNav.selectedItemId = R.id.nav_home
        }

        // calendar click
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_calendar) {
                showDateFilterMenu(toolbar)
                true
            } else false
        }
    }

    private fun showDateFilterMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add("Today")
        popup.menu.add("All Days")

        popup.menu.add("--- Last 5 Draw Dates ---").isEnabled = false

        // last 5 dates dynamically
        val lastFive = allResults
            .map { it.draw_date }
            .distinct()
            .takeLast(5)

        lastFive.forEach { popup.menu.add(it) }

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.title.toString()) {
                "Today" -> filterToday()
                "All Days" -> showAll()
                else -> filterByDate(menuItem.title.toString())
            }
            true
        }

        popup.show()
    }

    private fun loadResults() {
        db.collection("DrawResult")
            .orderBy("draw_date")
            .get()
            .addOnSuccessListener { snap ->
                allResults = snap.toObjects(DrawResult::class.java)
                showAll()
            }
    }

    private fun showAll() {
        recyclerView.adapter = ResultAdapter(allResults)
        tvEmpty.visibility = if (allResults.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun filterToday() {
        val today = android.text.format.DateFormat.format("dd MMM yyyy", java.util.Date()).toString()
        val filtered = allResults.filter { it.draw_date == today }
        recyclerView.adapter = ResultAdapter(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun filterByDate(date: String) {
        val filtered = allResults.filter { it.draw_date == date }
        recyclerView.adapter = ResultAdapter(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}
