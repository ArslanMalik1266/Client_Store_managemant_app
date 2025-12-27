package com.example.base44

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.base44.fragments.HomeFragment
import com.example.base44.fragments.ordersFragment
import com.example.base44.fragments.resultFragment
import com.example.base44.fragments.walletFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    internal lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.topAppBar)
        bottomNav = findViewById(R.id.bottomNavigation)
        navView = findViewById(R.id.nav_view)

        bottomNav.itemActiveIndicatorColor = ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.light_green)
        )


        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_cart) {
                BottomSheetCart()
                    .show(supportFragmentManager, "CartBottomSheet")
                true
            } else {
                false
            }
        }


        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
//            bottomNav.selectedItemId = R.id.nav_home
        }

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }

                R.id.nav_orders -> {
                    loadFragment(ordersFragment())
                    true
                }

                R.id.nav_result -> {
                    loadFragment(resultFragment())
                    true
                }

                R.id.nav_wallet -> {
                    loadFragment(walletFragment())
                    true
                }

                else -> false
            }
        }

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navHome -> bottomNav.selectedItemId = R.id.mainPage
                R.id.navOrders -> bottomNav.selectedItemId = R.id.nav_orders
                R.id.navResult -> bottomNav.selectedItemId = R.id.nav_result
                R.id.navWallet -> bottomNav.selectedItemId = R.id.nav_wallet
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun enableDrawer(enable: Boolean) {
        drawerLayout.setDrawerLockMode(
            if (enable)
                DrawerLayout.LOCK_MODE_UNLOCKED
            else
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        )

    }
}
