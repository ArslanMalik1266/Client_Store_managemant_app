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
            onEditCreditsClick = { user -> showEditCreditsDialog(user) },
            onItemClick = { user -> /* show user details if needed */ }
        )
        rvUsers.adapter = adapter
        rvUsers.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchUsersFromFirestore() {
        db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    usersList.clear()
                    for (doc in snapshot.documents) {
                        val user = User_for_admin(
                            id = doc.id,
                            fullName = doc.getString("username") ?: "",
                            email = doc.getString("email") ?: "",
                            currentBalance = (doc.getLong("walletBalance") ?: 0).toInt(),

                        )
                        usersList.add(user)
                    }

                    Log.d("AdminDashboard", "Users: ${usersList.map { it.fullName }}")
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

    private fun showEditCreditsDialog(user: User_for_admin) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_credits, null)
        val tvCurrentCredits = dialogView.findViewById<TextView>(R.id.tvCurrentCredits)
        val etAddCredits = dialogView.findViewById<EditText>(R.id.etAddCredits)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAddCredits)

        tvCurrentCredits.text = "Current Credits: ${user.currentBalance}"

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnAdd.setOnClickListener {
            val added = etAddCredits.text.toString().toIntOrNull() ?: 0
            if (added > 0) {
                user.currentBalance += added

                // Update in Firestore
                db.collection("users").document(user.id)
                    .update("walletBalance", user.currentBalance)

                adapter.notifyDataSetChanged()
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
