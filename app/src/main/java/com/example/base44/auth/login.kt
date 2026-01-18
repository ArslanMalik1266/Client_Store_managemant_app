package com.example.base44.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.base44.MainActivity
import com.example.base44.R
import com.example.base44.viewmodels.AuthViewModel
import com.example.base44.viewmodels.AuthViewModelFactory
import com.example.base44.repository.AuthRepository
import com.example.base44.adaptor.utils.SessionManager
import com.example.base44.network.RetrofitClient
import androidx.lifecycle.ViewModelProvider

class login : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleLogin: LinearLayout
    private lateinit var tvSignup: TextView
    private lateinit var session: SessionManager
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        session = SessionManager(this)
        
        if (session.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        initViewModel()
        initViews()
        setupListeners()
        observeViewModel()
    }

    private fun initViewModel() {
        val repo = AuthRepository()
        val factory = AuthViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        tvSignup = findViewById(R.id.tvSignup)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener { attemptLogin() }
        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { body ->
            btnLogin.isEnabled = true
            val actualToken = body?.getActualToken()

            android.util.Log.d("LOGIN_DEBUG", "Login Success! User: ${body?.user?.email}")

            session.saveLogin(
                role = body?.user?.role ?: "user",
                token = actualToken,
                username = body?.user?.fullName,
                email = body?.user?.email,
                userId = body?.user?.id
            )

            actualToken?.let { RetrofitClient.setAuthToken(it) }
            
            startActivity(Intent(this@login, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }

        viewModel.error.observe(this) { errorMsg ->
            btnLogin.isEnabled = true
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        btnLogin.isEnabled = false
        viewModel.login(email, password)
    }
}