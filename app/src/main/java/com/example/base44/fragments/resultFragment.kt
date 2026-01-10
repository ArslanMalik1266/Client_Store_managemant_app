package com.example.base44.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.MainActivity
import com.example.base44.R
import com.example.base44.adaptor.ResultAdapter
import com.example.base44.dataClass.ResultItem
import com.google.android.material.bottomnavigation.BottomNavigationView

class resultFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ResultAdapter

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

        val testList = listOf(
            ResultItem(
                title = "9Lotto",
                date = "30 Dec 2025 (Tue)",
                firstPrize = "2270",
                secondPrize = "1543",
                thirdPrize = "0745",
                specialNumbers = listOf(
                    "1543",
                    "0745",
                    "2511",
                    "9867",
                    "4412",
                    "8870",
                    "2221",
                    "7702",
                    "3314",
                    "9099"
                ),
                consolationNumbers = listOf(
                    "5543",
                    "8221",
                    "9921",
                    "5512",
                    "1120",
                    "8864",
                    "7710",
                    "3399",
                    "1666",
                    "7290"
                )
            ),
            ResultItem(
                title = "9Lotto",
                date = "30 Dec 2025 (Tue)",
                firstPrize = "2270",
                secondPrize = "1543",
                thirdPrize = "0745",
                specialNumbers = listOf(
                    "1543",
                    "0745",
                    "2511",
                    "9867",
                    "4412",
                    "8870",
                    "2221",
                    "7702",
                    "3314",
                    "9099"
                ),
                consolationNumbers = listOf(
                    "5543",
                    "8221",
                    "9921",
                    "5512",
                    "1120",
                    "8864",
                    "7710",
                    "3399",
                    "1666",
                    "7290"
                )
            ),
            ResultItem(
                title = "9Lotto",
                date = "30 Dec 2025 (Tue)",
                firstPrize = "2270",
                secondPrize = "1543",
                thirdPrize = "0745",
                specialNumbers = listOf(
                    "1543",
                    "0745",
                    "2511",
                    "9867",
                    "4412",
                    "8870",
                    "2221",
                    "7702",
                    "3314",
                    "9099"
                ),
                consolationNumbers = listOf(
                    "5543",
                    "8221",
                    "9921",
                    "5512",
                    "1120",
                    "8864",
                    "7710",
                    "3399",
                    "1666",
                    "7290"
                )
            ),
            ResultItem(
                title = "9Lotto",
                date = "30 Dec 2025 (Tue)",
                firstPrize = "2270",
                secondPrize = "1543",
                thirdPrize = "0745",
                specialNumbers = listOf(
                    "1543",
                    "0745",
                    "2511",
                    "9867",
                    "4412",
                    "8870",
                    "2221",
                    "7702",
                    "3314",
                    "9099"
                ),
                consolationNumbers = listOf(
                    "5543",
                    "8221",
                    "9921",
                    "5512",
                    "1120",
                    "8864",
                    "7710",
                    "3399",
                    "1666",
                    "7290"
                )
            ),
            ResultItem(
                title = "9Lotto",
                date = "30 Dec 2025 (Tue)",
                firstPrize = "2270",
                secondPrize = "1543",
                thirdPrize = "0745",
                specialNumbers = listOf(
                    "1543",
                    "0745",
                    "2511",
                    "9867",
                    "4412",
                    "8870",
                    "2221",
                    "7702",
                    "3314",
                    "9099"
                ),
                consolationNumbers = listOf(
                    "5543",
                    "8221",
                    "9921",
                    "5512",
                    "1120",
                    "8864",
                    "7710",
                    "3399",
                    "1666",
                    "7290"
                )
            )
        )

        adapter = ResultAdapter(testList)
        recyclerView.adapter = adapter

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
    }
}
