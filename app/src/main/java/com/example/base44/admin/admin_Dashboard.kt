package com.example.base44.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.adaptor.UserAdapte_admin
import com.example.base44.auth.login
import com.example.base44.dataClass.User_for_admin
import com.google.android.material.navigation.NavigationView

class admin_Dashboard : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var rvUsers: RecyclerView
    private lateinit var logoutButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)
        rvUsers = findViewById(R.id.rvUsers)
        logoutButton = findViewById(R.id.btnLogout)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        val recyclerView: RecyclerView = findViewById(R.id.rvUsers)
        val users = listOf(
            User_for_admin("1", "Arslan Malik", "arslan@example.com", 1000, 500),
            User_for_admin("2", "Ali Khan", "ali@example.com", 1500, 750)
        )

        val adapter = UserAdapte_admin(users,
            onEditCreditsClick = { user ->
                // Show dialog to edit credits
            },
            onItemClick = { user ->
                // Show user details dialog
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        logoutButton.setOnClickListener {
            logout()
        }

    }

    private fun logout() {
        val intent = Intent(this, login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}