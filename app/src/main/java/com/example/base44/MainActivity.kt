package com.example.base44

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.base44.adaptor.utils.SessionManager
import com.example.base44.auth.login
import com.example.base44.dataClass.CartManager
import com.example.base44.dataClass.Product
import com.example.base44.dataClass.api.UserData
import com.example.base44.fragments.HomeFragment
import com.example.base44.fragments.ordersFragment
import com.example.base44.fragments.resultFragment
import com.example.base44.fragments.walletFragment
import com.example.base44.network.RetrofitClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    internal lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navView: NavigationView
    private lateinit var logoutBtn: Button
    private val selectedProducts = mutableListOf<Product>()
    private lateinit var btnAddToCartTop: Button
    private lateinit var session: SessionManager
    var userAvailableBalance: Double = 0.0
    var currentUser: UserData? = null

    override fun onStart() {
        super.onStart()
        if (!session.isLoggedIn()) {
            startActivity(Intent(this, login::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        session = SessionManager(this)

        session.getToken()?.let { token ->
            RetrofitClient.setAuthToken(token)
        }

        initViews()
        setupToolbar()
        setupBottomNav()
        setupNavView()
        setupTopCartButton()
        logoutAccount()
        fetchUserProfile()

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    fun fetchUserProfile() {
        val userId = session.getUserId()
        if (userId.isNullOrEmpty()) return

        RetrofitClient.instance.getProfile(userId).enqueue(object : retrofit2.Callback<UserData> {
            override fun onResponse(
                call: retrofit2.Call<UserData>,
                response: retrofit2.Response<UserData>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    currentUser = user

                    // Update UI
                    val headerView = navView.getHeaderView(0)
                    val tvDrawerUsername = headerView.findViewById<TextView>(R.id.tvUsername)
                    val tvDrawerBalance = headerView.findViewById<TextView>(R.id.tvBalance)

                    tvDrawerUsername.text = user.fullName ?: user.username ?: "User"

                    // Update Balance
                    userAvailableBalance = user.currentBalance ?: 0.0
                    tvDrawerBalance.text = "Balance: RM %.2f".format(userAvailableBalance)
                    android.util.Log.d("MAIN_ACTIVITY", "Balance updated: $userAvailableBalance")
                }
            }

            override fun onFailure(call: retrofit2.Call<UserData>, t: Throwable) {
                android.util.Log.e("MAIN_ACTIVITY", "Failed to fetch profile", t)
            }
        })
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.topAppBar)
        bottomNav = findViewById(R.id.bottomNavigation)
        navView = findViewById(R.id.nav_view)
        logoutBtn = findViewById(R.id.btnLogout)
        btnAddToCartTop = findViewById(R.id.btnAddToCartTop)
        btnAddToCartTop.visibility = View.GONE

        val headerView = navView.getHeaderView(0)
        val tvDrawerUsername = headerView.findViewById<TextView>(R.id.tvUsername)

        val username = session.getUsername() ?: "User"
        tvDrawerUsername.text = username

        userAvailableBalance = 0.0
    }

    private fun setupToolbar() {
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_cart) {
                currentUser?.let { user ->
                    BottomSheetCart(user)
                        .show(supportFragmentManager, "CartBottomSheet")
                } ?: run {
                    Toast.makeText(this, "User profile loading... please wait", Toast.LENGTH_SHORT)
                        .show()
                    fetchUserProfile()
                }
                true
            } else false
        }

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupBottomNav() {
        bottomNav.itemActiveIndicatorColor = ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.light_green)
        )

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_orders -> loadFragment(ordersFragment())
                R.id.nav_result -> loadFragment(resultFragment())
                R.id.nav_wallet -> loadFragment(walletFragment())
                else -> return@setOnItemSelectedListener false
            }
            true
        }
    }

    private fun setupNavView() {
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navHome -> bottomNav.selectedItemId = R.id.nav_home
                R.id.navOrders -> bottomNav.selectedItemId = R.id.nav_orders
                R.id.navResult -> bottomNav.selectedItemId = R.id.nav_result
                R.id.navWallet -> bottomNav.selectedItemId = R.id.nav_wallet
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupTopCartButton() {
        btnAddToCartTop.setOnClickListener {
            if (selectedProducts.isNotEmpty()) {
                MyBottomSheet(selectedProducts).show(supportFragmentManager, "AddToCartBottomSheet")
            }
        }
    }

    fun showTopAddToCartButton(show: Boolean) {
        btnAddToCartTop.visibility = if (show) View.VISIBLE else View.GONE
        toolbar.title = if (show) "" else "Golden Sparrow"
    }

    fun updateSelectedItems(products: List<Product>) {
        selectedProducts.clear()
        selectedProducts.addAll(products)
        showTopAddToCartButton(selectedProducts.isNotEmpty())
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun enableDrawer(enable: Boolean) {
        drawerLayout.setDrawerLockMode(
            if (enable) DrawerLayout.LOCK_MODE_UNLOCKED
            else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        )
    }

    private fun logoutAccount() {
        logoutBtn.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    session.logout()
                    startActivity(Intent(this, login::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
}

