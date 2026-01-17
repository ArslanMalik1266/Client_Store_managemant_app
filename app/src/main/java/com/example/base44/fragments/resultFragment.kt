package com.example.base44.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.MainActivity
import com.example.base44.R
import com.example.base44.adaptor.ResultAdapter
import com.example.base44.dataClass.ResultItem
import com.example.base44.network.RetrofitClient
import com.example.base44.repository.DrawRepository
import com.example.base44.viewmodels.DrawViewModel
import com.example.base44.viewmodels.DrawViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView

class resultFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ResultAdapter
    private lateinit var viewModel: DrawViewModel

    override fun onResume() {
        super.onResume()
        hideToolbarAndDrawer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)
        setupToolbar(view)

        recyclerView = view.findViewById(R.id.recyclerResults)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        
        val progressBar = view.findViewById<android.widget.ProgressBar>(R.id.progressBar)

        // Adapter initialized with empty list
        adapter = ResultAdapter(emptyList())
        recyclerView.adapter = adapter

        // ViewModel
        val api = RetrofitClient.api
        val repo = DrawRepository(api)
        val factory = DrawViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[DrawViewModel::class.java]

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // Observe results
        viewModel.results.observe(viewLifecycleOwner) { results ->
            adapter.updateData(results)
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_LONG).show()
            }
        }

        // Observe available dates for filtering
        viewModel.availableDates.observe(viewLifecycleOwner) {
            // Do nothing here, used in popup dynamically
        }

        viewModel.loadResults()

        return view
    }

    private fun hideToolbarAndDrawer() {
        val activity = activity as MainActivity
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

            val bottomNav =
                requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
            bottomNav.selectedItemId = R.id.nav_home
        }

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_calendar -> {
                    val menuItemView = toolbar.findViewById<View>(R.id.action_calendar)
                    showFilterPopup(menuItemView ?: toolbar)
                    true
                }
                else -> false
            }
        }
    }

    private fun showFilterPopup(anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView, android.view.Gravity.END)

        // Always add "All"
        popup.menu.add(0, 0, 0, "All")

        // Add dynamic dates from ViewModel
        val dates = viewModel.availableDates.value ?: emptyList()
        dates.forEachIndexed { index, date ->
            popup.menu.add(0, index + 1, index + 1, date)
        }

        popup.setOnMenuItemClickListener { item ->
            val selectedFilter = item.title.toString()
            Toast.makeText(requireContext(), "Filter: $selectedFilter", Toast.LENGTH_SHORT).show()

            // Apply filter to results
            val filteredResults = if (selectedFilter == "All") {
                viewModel.results.value
            } else {
                viewModel.results.value?.filter { it.date == selectedFilter }
            }
            filteredResults?.let { adapter.updateData(it) }

            true
        }

        popup.show()
    }
}
