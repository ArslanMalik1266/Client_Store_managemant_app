package com.example.base44

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.base44.fragments.HomeFragment
import com.example.base44.fragments.ordersFragment
import com.example.base44.fragments.resultFragment
import com.example.base44.fragments.walletFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        //bottom navigation ka code
        bottomNav = findViewById(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_result -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, resultFragment())
                        .commit()
                    true
                }
                R.id.nav_wallet -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, walletFragment())
                        .commit()
                    true
                }
                R.id.nav_orders -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ordersFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }


        topAppBar = findViewById(R.id.topAppBar)
//        topAppBar.setOnMenuItemClickListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.action_cart -> {
//                    // Open Cart Activity
//                    true
//                }
//                else -> false
//            }
//        }
    }
}