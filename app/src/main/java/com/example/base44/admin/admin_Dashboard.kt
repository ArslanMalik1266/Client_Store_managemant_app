package com.example.base44.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.adaptor.UserAdapte_admin
import com.example.base44.adaptor.utils.SessionManager
import com.example.base44.admin.fragments.AdminDrawResultFragment
import com.example.base44.auth.login
import com.example.base44.dataClass.User_for_admin
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore

class admin_Dashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var rvUsers: RecyclerView
    private lateinit var logoutButton: Button
    private lateinit var session: SessionManager
    private lateinit var db: FirebaseFirestore

    private var usersList = mutableListOf<User_for_admin>()
    private var fullUsersList = mutableListOf<User_for_admin>()
    private var currentSearchQuery: String = ""
    private var currentStatusFilter: String = "All"
    private lateinit var adapter: UserAdapte_admin

    override fun onStart() {
        super.onStart()
        if (!session.isLoggedIn() || session.getRole() != "admin") {
            startActivity(Intent(this, login::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        session = SessionManager(this)
        db = FirebaseFirestore.getInstance()

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)
        rvUsers = findViewById(R.id.rvUsers)
        logoutButton = findViewById(R.id.btnLogout)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setCheckedItem(R.id.navAdminUsers)

        setupRecyclerView()
        setupSearchView()
        setupSpinner()
        fetchUsersFromFirestore()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navAdminUsers -> {
                    showDashboardContent(true)
                    supportActionBar?.title = "Admin Management"
                }
                R.id.navAdminDrawResult -> {
                    showDashboardContent(false)
                    supportActionBar?.title = "Draw Results"
                    loadFragment(AdminDrawResultFragment())
                }
                // Add more cases here for other fragments
            }
            drawerLayout.closeDrawers()
            true
        }

        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun setupSearchView() {
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchUsers)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                performFilter()
                return true
            }
        })
    }

    private fun setupSpinner() {
        val spinner = findViewById<Spinner>(R.id.spinnerStatusFilter)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.user_status_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentStatusFilter = parent?.getItemAtPosition(position).toString()
                performFilter()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun performFilter() {
        var filtered: List<User_for_admin> = fullUsersList

        // Apply Search Filter
        if (currentSearchQuery.isNotEmpty()) {
            filtered = filtered.filter { 
                it.fullName.contains(currentSearchQuery, ignoreCase = true) || 
                it.email.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        // Apply Status Filter
        if (currentStatusFilter != "All") {
            val targetActive = currentStatusFilter == "Active"
            filtered = filtered.filter { it.canWork == targetActive }
        }

        adapter.updateList(filtered)
        
        // Update user count display based on filtered results
        findViewById<TextView>(R.id.tvListHeader).text = "Registered Users (${filtered.size})"
    }

    private fun setupRecyclerView() {
        adapter = UserAdapte_admin(
            usersList,
            onEditCreditsClick = { user -> showQuickEditDialog(user) },
            onItemClick = { user -> showUserDetailsDialog(user) }
        )
        rvUsers.adapter = adapter
        rvUsers.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchUsersFromFirestore() {
        db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AdminDashboard", "Firestore error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    fullUsersList.clear()
                    var platformTotalSales = 0.0
                    var platformTotalCredit = 0.0
                    
                    for (doc in snapshot.documents) {
                        val userTotalSales = when (val sales = doc.get("totalSales")) {
                            is Double -> sales
                            is Long -> sales.toDouble()
                            else -> 0.0
                        }
                        platformTotalSales += userTotalSales

                        val userCreditLimit = when (val lim = doc.get("creditLimit")) {
                            is Double -> lim
                            is Long -> lim.toDouble()
                            else -> 0.0
                        }
                        platformTotalCredit += userCreditLimit

                        val user = User_for_admin(
                            id = doc.id,
                            fullName = doc.getString("username") ?: "",
                            email = doc.getString("email") ?: "",
                            currentBalance = when (val bal = doc.get("walletBalance")) {
                                is Double -> bal
                                is Long -> bal.toDouble()
                                else -> 0.0
                            },
                            creditLimit = userCreditLimit,
                            totalSales = userTotalSales,
                            adminCredits = (doc.getLong("adminCredits") ?: 0).toInt(),
                            canWork = doc.getBoolean("canWork") ?: true
                        )
                        fullUsersList.add(user)
                    }

                    // Update Dashboard Stats
                    findViewById<TextView>(R.id.tvTotalUsersCount).text = fullUsersList.size.toString()
                    findViewById<TextView>(R.id.tvPlatformTotalSales).text = "RM %.2f".format(platformTotalSales)
                    findViewById<TextView>(R.id.tvPlatformTotalCredit).text = "RM %.2f".format(platformTotalCredit)
                    
                    val platformTotalCommission = platformTotalSales * 0.25
                    findViewById<TextView>(R.id.tvPlatformTotalCommission).text = "RM %.2f".format(platformTotalCommission)

                    // Apply current filter to update the adapter
                    performFilter()
                }
            }
    }

    private fun logout() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                session.logout()
                val intent = Intent(this, login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showQuickEditDialog(user: User_for_admin) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_credit, null)
        val tvName = dialogView.findViewById<TextView>(R.id.dialogUserName)
        val tvCurrentBalance = dialogView.findViewById<TextView>(R.id.tvCurrentAvailableBalance)
        val etWalletLimit = dialogView.findViewById<EditText>(R.id.etWalletLimit)
        val btnReset = dialogView.findViewById<Button>(R.id.btnResetBalance)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveWallet)

        tvName.text = user.fullName
        tvCurrentBalance.text = "Current Balance: RM %.2f".format(user.currentBalance)
        etWalletLimit.setText(user.creditLimit.toString())

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        var pendingNewBalance = user.currentBalance

        btnReset.setOnClickListener {
            val limit = etWalletLimit.text.toString().toDoubleOrNull() ?: 0.0
            pendingNewBalance = limit
            tvCurrentBalance.text = "New Balance: RM %.2f (Reset Applied)".format(pendingNewBalance)
            tvCurrentBalance.setTextColor(android.graphics.Color.RED)
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val newLimit = etWalletLimit.text.toString().toDoubleOrNull() ?: user.creditLimit
            
            val updates = HashMap<String, Any>()
            updates["creditLimit"] = newLimit
            updates["walletBalance"] = pendingNewBalance

            db.collection("users").document(user.id)
                .update(updates)
                .addOnSuccessListener {
                    dialog.dismiss()
                    android.widget.Toast.makeText(this, "Wallet updated for ${user.fullName}", android.widget.Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("AdminDashboard", "Failed to update wallet: ${e.message}")
                }
        }

        dialog.show()
    }



    private fun showUserDetailsDialog(user: User_for_admin) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user_details, null)

        val tvUserInitial = dialogView.findViewById<TextView>(R.id.tvUserInitial_dialog)
        val tvName = dialogView.findViewById<TextView>(R.id.tvUserName)
        val tvEmail = dialogView.findViewById<TextView>(R.id.tvUserEmail)
        val tvBalance = dialogView.findViewById<TextView>(R.id.tvBalance)
        val tvWalletLimit = dialogView.findViewById<TextView>(R.id.tvWalletLimit)
        val tvTotalSales = dialogView.findViewById<TextView>(R.id.tvTotalSales)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tvUserStatus)
        val switchCanWork = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchCanWork)
        val btnEdit = dialogView.findViewById<Button>(R.id.btnEditUser)

        tvUserInitial.text = user.fullName.firstOrNull()?.uppercase() ?: "U"
        tvName.text = user.fullName
        tvEmail.text = user.email
        tvBalance.text = "Balance: RM %.2f".format(user.currentBalance)
        tvWalletLimit.text = "Limit: RM %.2f".format(user.creditLimit)
        tvTotalSales.text = "Total Sales: RM %.2f".format(user.totalSales)
        
        fun updateStatusUI(isActive: Boolean) {
            if (isActive) {
                tvStatus.text = "Status: Active"
                tvStatus.setTextColor(android.graphics.Color.parseColor("#10B981")) // Emerald Green
            } else {
                tvStatus.text = "Status: Suspended"
                tvStatus.setTextColor(android.graphics.Color.RED)
            }
        }

        updateStatusUI(user.canWork)
        switchCanWork.isChecked = user.canWork

        switchCanWork.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "active" else "suspended"
            val updates = mapOf(
                "canWork" to isChecked,
                "status" to status
            )

            db.collection("users").document(user.id)
                .update(updates)
                .addOnSuccessListener {
                    user.canWork = isChecked
                    updateStatusUI(isChecked)
                    val msg = if (isChecked) "Account Activated" else "Account Suspended"
                    android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    android.widget.Toast.makeText(this, "Failed to update status", android.widget.Toast.LENGTH_SHORT).show()
                }
        }

        // Edit button inside dialog → open quick edit dialog
        btnEdit.setOnClickListener {
            showQuickEditDialog(user)
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showDashboardContent(show: Boolean) {
        val dashboardContent = findViewById<View>(R.id.adminDashboardContent)
        val fragmentContainer = findViewById<View>(R.id.adminFragmentContainer)
        
        if (show) {
            dashboardContent.visibility = View.VISIBLE
            fragmentContainer.visibility = View.GONE
            // Remove any existing fragment
            supportFragmentManager.findFragmentById(R.id.adminFragmentContainer)?.let {
                supportFragmentManager.beginTransaction().remove(it).commit()
            }
        } else {
            dashboardContent.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.adminFragmentContainer, fragment)
            .commit()
    }
}
