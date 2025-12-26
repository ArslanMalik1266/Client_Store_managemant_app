package com.example.base44.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.BottomSheetCart
import com.example.base44.MyBottomSheet
import com.example.base44.R
import com.example.base44.adaptor.ProductAdapter
import com.example.base44.dataClass.Product
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView


class HomeFragment : Fragment() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private val productList = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val navView = view.findViewById<NavigationView>(R.id.nav_view)


        topAppBar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        drawerLayout = view.findViewById<DrawerLayout>(R.id.drawerLayout)
        rvProducts = view.findViewById(R.id.rvProducts)
        bottomNavigationView =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)


        topAppBar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }


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
            val bottomSheet = MyBottomSheet(product.title)
            bottomSheet.show(parentFragmentManager, "MyBottomSheet")
        }

        rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        rvProducts.adapter = productAdapter

        topAppBar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_cart) {
                val bottomSheet = BottomSheetCart()
                bottomSheet.show(parentFragmentManager, "CartBottomSheet")
                true
            } else false
        }


        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navHome -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    bottomNavigationView.selectedItemId = R.id.nav_home
                    true
                }
                R.id.navOrders -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ordersFragment())
                        .commit()
                    bottomNavigationView.selectedItemId = R.id.nav_orders
                    true
                }

                R.id.navResult -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, resultFragment())
                        .commit()
                    bottomNavigationView.selectedItemId = R.id.nav_result
                    true
                }

                R.id.navWallet -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, walletFragment())
                        .commit()
                    bottomNavigationView.selectedItemId = R.id.nav_wallet
                    true

                }

                else -> false

            }
        }

        return view
    }

}