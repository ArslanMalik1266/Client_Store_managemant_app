package com.example.base44.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.adaptor.UserAdapte_admin
import com.example.base44.adaptor.utils.SessionManager
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

        setupRecyclerView()
        fetchUsersFromFirestore()

        logoutButton.setOnClickListener {
            logout()
        }
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
                    usersList.clear()
                    for (doc in snapshot.documents) {
                        val user = User_for_admin(
                            id = doc.id,
                            fullName = doc.getString("username") ?: "",
                            email = doc.getString("email") ?: "",
                            currentBalance = (doc.getLong("walletBalance") ?: 0).toInt(),
                            creditLimit = (doc.getLong("creditLimit") ?: 0).toInt(),   // Added
                            totalSales = (doc.getLong("totalSales") ?: 0).toInt()
                        )
                        usersList.add(user)
                    }

                    Log.d("AdminDashboard", "Fetched users: ${usersList.map { it.fullName }}")
                    adapter.notifyDataSetChanged()
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
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_credits, null)
        val tvCurrentCredits = dialogView.findViewById<TextView>(R.id.tvCurrentCredits)
        val etAddCredits = dialogView.findViewById<EditText>(R.id.etAddCredits)

        // Naye EditTexts for CreditLimit & TotalSales
        val etCreditLimit = dialogView.findViewById<EditText>(R.id.etCreditLimit)
        val etTotalSales = dialogView.findViewById<EditText>(R.id.etTotalSales)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAddCredits)

        // Set initial values
        tvCurrentCredits.text = "Current Balance: ${user.currentBalance}"
        etAddCredits.setText(user.currentBalance.toString())
        etCreditLimit.setText(user.creditLimit.toString())
        etTotalSales.setText(user.totalSales.toString())

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnAdd.setOnClickListener {
            // Read values from EditTexts
            val newBalance = etAddCredits.text.toString().toIntOrNull() ?: 0
            val newCreditLimit = etCreditLimit.text.toString().toIntOrNull() ?: 0
            val newTotalSales = etTotalSales.text.toString().toIntOrNull() ?: 0

            // Update local object
            user.currentBalance = newBalance
            user.creditLimit = newCreditLimit
            user.totalSales = newTotalSales

            // Update Firestore
            db.collection("users").document(user.id)
                .update(
                    mapOf(
                        "walletBalance" to user.currentBalance,
                        "creditLimit" to user.creditLimit,
                        "totalSales" to user.totalSales
                    )
                ).addOnSuccessListener {
                    adapter.notifyDataSetChanged()
                }.addOnFailureListener { e ->
                    Log.e("AdminDashboard", "Failed to update user: ${e.message}")
                }

            dialog.dismiss()
        }

        dialog.show()
    }



    private fun showUserDetailsDialog(user: User_for_admin) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user_details, null)

        val tvName = dialogView.findViewById<TextView>(R.id.tvUserName)
        val tvEmail = dialogView.findViewById<TextView>(R.id.tvUserEmail)
        val tvBalance = dialogView.findViewById<TextView>(R.id.tvBalance)
        val tvCreditLimit = dialogView.findViewById<TextView>(R.id.tvCreditLimit)
        val tvTotalSales = dialogView.findViewById<TextView>(R.id.tvTotalSales)
        val btnEdit = dialogView.findViewById<Button>(R.id.btnEditUser)

        tvName.text = user.fullName
        tvEmail.text = user.email
        tvBalance.text = "Current Balance: ${user.currentBalance}"
        tvCreditLimit.text = "Credit Limit: ${user.creditLimit}"
        tvTotalSales.text = "Total Sales: ${user.totalSales}"

        // Edit button inside dialog → open quick edit dialog
        btnEdit.setOnClickListener {
            showQuickEditDialog(user)
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }


}
