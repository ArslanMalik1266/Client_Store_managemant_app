package com.example.base44.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.MainActivity
import com.example.base44.MyBottomSheet
import com.example.base44.R
import com.example.base44.adaptor.ProductAdapter
import com.example.base44.dataClass.Product
import com.example.base44.dataClass.api.ProductEntity
import com.example.base44.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()

    override fun onResume() {
        super.onResume()
        showToolbarAndDrawer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        initRecyclerView(view)
        setupAdapter()
        
        // Debug: Check if token is set
        val session = com.example.base44.adaptor.utils.SessionManager(requireContext())
        val token = session.getToken()
        android.util.Log.d("PRODUCT_API", "Token status: ${if (token.isNullOrEmpty()) "NO TOKEN" else "Token exists (${token.take(10)}...)"}")
        
        loadProducts() // Fetch products from Base44 API

        return view
    }

    private fun initRecyclerView(view: View) {
        rvProducts = view.findViewById(R.id.rvProducts)
        rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun setupAdapter() {
        productAdapter = ProductAdapter(
            products = productList,
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

    private fun loadProducts() {
        productList.clear()

        RetrofitClient.instance.getProducts().enqueue(object : Callback<List<ProductEntity>> {
            override fun onResponse(
                call: Call<List<ProductEntity>>,
                response: Response<List<ProductEntity>>
            ) {
                Log.d("PRODUCT_API", "Response code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!
                    Log.d("PRODUCT_API", "Products count: ${products.size}")
                    
                    if (products.isEmpty()) {
                        Log.e("PRODUCT_API", "Products list is empty")
                        loadDummyProducts()
                        return
                    }

                    products.forEach { apiProduct ->
                        Log.d("PRODUCT_API", "Product: name=${apiProduct.name}, code=${apiProduct.code}, image=${apiProduct.image}")
                        productList.add(
                            Product(
                                code = apiProduct.code ?: apiProduct.id ?: "",
                                title = apiProduct.name ?: apiProduct.productTitle ?: "Unnamed Product",
                                drawableName = apiProduct.image ?: apiProduct.imageUrl ?: ""
                            )
                        )
                    }

                    productAdapter.notifyDataSetChanged()
                    Toast.makeText(
                        requireContext(),
                        "Loaded ${products.size} products",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PRODUCT_API", "API failed with code ${response.code()}: $errorBody")
                    Toast.makeText(requireContext(), "API Error: ${response.code()}", Toast.LENGTH_LONG).show()
                    loadDummyProducts()
                }
            }

            override fun onFailure(call: Call<List<ProductEntity>>, t: Throwable) {
                Log.e("PRODUCT_API", "Network error: ${t.message}", t)
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                loadDummyProducts()
            }
        })
    }

    private fun loadDummyProducts() {
        productList.add(Product("Wh-1001", "Kuda", "headphones_image"))
        productList.add(Product("SW-2045", "Todo", "watch_image"))
        productList.add(Product("LB-3022", "Magnum", "bag_image"))
        productAdapter.notifyDataSetChanged()
    }

    private fun showToolbarAndDrawer() {
        val activity = activity as MainActivity
        activity.toolbar.visibility = View.VISIBLE
        activity.enableDrawer(true)
    }
}
