package com.example.base44.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.adaptor.ProductAdapter
import com.example.base44.dataClass.Product


class HomeFragment : Fragment() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val view = inflater.inflate(R.layout.fragment_home, container, false)

        rvProducts = view.findViewById(R.id.rvProducts)

        productList.add(Product("Wh-1001", "Kuda", R.drawable.headphones_image))
        productList.add(Product("SW-2045", "Todo", R.drawable.watch_image))
        productList.add(Product("LB-3022", "Magnum", R.drawable.bag_image))
        productList.add(Product("DL-4015", "SG Pools", R.drawable.lamp_photo))
        productList.add(Product("CS-5008", "Sabah", R.drawable.cup_image))
        productList.add(Product("MK-6033", "SG Pools", R.drawable.keyboard_image))
        productList.add(Product("PS-7019", "Dragon", R.drawable.speaker_image))
        productList.add(Product("SG-8027", "Lotto", R.drawable.glasses_image))
        productList.add(Product("FT-9041", "9Lotto", R.drawable.watch_image_2))


        productAdapter = ProductAdapter(productList) { product ->
            // Yeh lambda tab chalega jab Add ya checkbox click hoga
            // Yahan aap cart me item add karne ka code dal sakte ho
        }

        // RecyclerView set karo
        rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        rvProducts.adapter = productAdapter

        return view
    }

}