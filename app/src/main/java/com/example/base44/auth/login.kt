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
        
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        tvSignup = findViewById(R.id.tvSignup)
        session = SessionManager(this)

        if (intent.getBooleanExtra("suspended", false)) {
            Toast.makeText(this, "Account Suspended", Toast.LENGTH_LONG).show()
        }

        if (session.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        tvSignup.setOnClickListener {
            val intent = Intent(this, signUp::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            val loginRequest = LoginRequest(email, password)
            RetrofitClient.instance.login(loginRequest).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    btnLogin.isEnabled = true
                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        if (authResponse?.status == "success") {
                            val user = authResponse.user
                            session.saveLogin(
                                role = user?.role ?: "user",
                                token = authResponse.token,
                                username = user?.fullName,
                                email = user?.email
                            )
                            val intent = Intent(this@login, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            Toast.makeText(this@login, "Login successful", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@login, authResponse?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown Error"
                        
                        // Check if error is related to verification
                        if (errorBody.contains("verify", ignoreCase = true)) {
                            android.app.AlertDialog.Builder(this@login)
                                .setTitle("Verification Required")
                                .setMessage("Please verify your email address to login.\nReason: $errorBody")
                                .setPositiveButton("Verify Now") { _, _ ->
                                    val intent = Intent(this@login, VerificationActivity::class.java)
                                    intent.putExtra("EMAIL", email)
                                    // Token might not be available here, but verify endpoint might work with just email/code or we prompt re-signup?
                                    // Assuming VerificationActivity can handle just email if needed, or user will re-request code
                                    startActivity(intent)
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        } else {
                            android.app.AlertDialog.Builder(this@login)
                                .setTitle("Login Failed")
                                .setMessage("Code: ${response.code()}\nReason: $errorBody")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    btnLogin.isEnabled = true
                    Toast.makeText(this@login, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

}