package com.example.base44.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.MainActivity
import com.example.base44.MyBottomSheet
import com.example.base44.R
import com.example.base44.adaptor.ProductAdapter
import com.example.base44.network.RetrofitClient
import com.example.base44.repository.ProductsRepository
import com.example.base44.viewmodels.ProductsViewModel
import com.example.base44.viewmodels.ProductsViewModelFactory

class HomeFragment : Fragment() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var progressBar: ProgressBar

    // ViewModel setup
    private val viewModel: ProductsViewModel by viewModels {
        ProductsViewModelFactory(ProductsRepository(RetrofitClient.api))
    }

    override fun onResume() {
        super.onResume()
        showToolbarAndDrawer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        initViews(view)
        setupRecyclerView()
        observeViewModel()

        // Load products
        viewModel.loadProducts()

        return view
    }

    private fun initViews(view: View) {
        rvProducts = view.findViewById(R.id.rvProducts)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupRecyclerView() {
        rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        rvProducts.setHasFixedSize(true) // Optimizes layout for large lists

        productAdapter = ProductAdapter(
            products = mutableListOf(),
            onAddToCartClicked = { product ->
                MyBottomSheet(listOf(product))
                    .show(parentFragmentManager, "MyBottomSheet")
            },
            onSelectionChanged = { selectedProducts ->
                (requireActivity() as MainActivity).updateSelectedItems(selectedProducts)
            }
        )
        rvProducts.adapter = productAdapter
    }

    private fun observeViewModel() {

        // Observe Product List
        viewModel.productList.observe(viewLifecycleOwner, Observer { list ->
            list?.let {
                // Use DiffUtil inside ProductAdapter for smooth updates
                productAdapter.updateList(it)
            }
        })

        // Observe Loading State
        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            rvProducts.visibility = if (isLoading) View.GONE else View.VISIBLE
        })

        // Observe Error
        viewModel.error.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showToolbarAndDrawer() {
        val activity = activity as MainActivity
        activity.toolbar.visibility = View.VISIBLE
        activity.enableDrawer(true)
    }
}
