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
import com.example.base44.adaptor.utils.SessionManager
import com.example.base44.dataClass.api.AuthResponse
import com.example.base44.dataClass.api.LoginRequest
import com.example.base44.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class login : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleLogin: LinearLayout
    private lateinit var tvSignup: TextView
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        session = SessionManager(this)

        // Check if already logged in
        if (session.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        initViews()
        setupListeners()
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

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        btnLogin.isEnabled = false

        val request = LoginRequest(email, password)
        RetrofitClient.instance.login(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                btnLogin.isEnabled = true

                // Debug: Print raw response
                val rawResponse = response.raw().toString()
                android.util.Log.d("LOGIN_DEBUG", "Response Code: ${response.code()}")
                android.util.Log.d("LOGIN_DEBUG", "Response Body: ${response.body()}")
                android.util.Log.d("LOGIN_DEBUG", "Token: ${response.body()?.token}")
                android.util.Log.d("LOGIN_DEBUG", "AccessToken: ${response.body()?.accessToken}")
                android.util.Log.d("LOGIN_DEBUG", "ActualToken: ${response.body()?.getActualToken()}")
                android.util.Log.d("LOGIN_DEBUG", "Raw: $rawResponse")

                if (response.isSuccessful && (response.body()?.status == "success" || response.code() == 200)) {
                    val body = response.body()
                    val actualToken = body?.getActualToken()

                    session.saveLogin(
                        role = body?.user?.role ?: "user",
                        token = actualToken,
                        username = body?.user?.fullName,
                        email = body?.user?.email,
                        userId = body?.user?.id
                    )
                    
                    // Set token for API calls
                    actualToken?.let { RetrofitClient.setAuthToken(it) }
                    android.util.Log.d("LOGIN_DEBUG", "Token saved and set: ${actualToken?.take(20)}...")

                    Toast.makeText(this@login, "Login successful", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@login, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    finish()
                } else {
                    val body = response.body()
                    val status = body?.status ?: "null"
                    val message = body?.message ?: "No message"

                    Toast.makeText(this@login, "Login failed: $message", Toast.LENGTH_LONG).show()
                    android.util.Log.e("LOGIN_DEBUG", "Login failed - Code: ${response.code()}, Status: $status, Msg: $message")
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                btnLogin.isEnabled = true
                Toast.makeText(this@login, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}