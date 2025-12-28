package com.example.base44

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.base44.auth.login
import com.example.base44.fragments.HomeFragment
import com.example.base44.fragments.ordersFragment
import com.example.base44.fragments.resultFragment
import com.example.base44.fragments.walletFragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.log

class MainActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var drawerLayout: DrawerLayout
    internal lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navView: NavigationView
    private lateinit var logoutBtn: Button
    private lateinit var googleSignInClient: GoogleSignInClient
    private val selectedItems = mutableListOf<String>()
    private lateinit var btnAddToCartTop: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.topAppBar)
        bottomNav = findViewById(R.id.bottomNavigation)
        navView = findViewById(R.id.nav_view)
        logoutBtn = findViewById(R.id.btnLogout)

        btnAddToCartTop = findViewById(R.id.btnAddToCartTop)
        btnAddToCartTop.visibility = View.GONE


        btnAddToCartTop.setOnClickListener {
            // Open BottomSheet for selected items
            val itemsString = selectedItems.joinToString(", ")
            val bottomSheet = MyBottomSheet(itemsString)
            bottomSheet.show(supportFragmentManager, "AddToCartBottomSheet")
        }

        val headerView = navView.getHeaderView(0)  // header layout ka view
        val tvDrawerUsername = headerView.findViewById<TextView>(R.id.tvUsername)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val username = document.getString("username")
                        tvDrawerUsername.text = username ?: "User"
                    }
                }
                .addOnFailureListener {
                    tvDrawerUsername.text = "User"
                }

        }


        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso)



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
        logoutAccount()
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

    private fun logoutAccount() {
        logoutBtn.setOnClickListener {

            MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    auth.signOut()
                    googleSignInClient.signOut()

                    val intent = Intent(this, login::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                    finish()
                }

                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }

                .show()
        }
    }

}
