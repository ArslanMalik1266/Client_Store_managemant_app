package com.example.base44.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.base44.MainActivity
import com.example.base44.R
import com.example.base44.adaptor.utils.SessionManager
import com.example.base44.dataClass.api.AuthResponse
import com.example.base44.dataClass.api.RegisterRequest
import com.example.base44.dataClass.api.UserData
import com.example.base44.dataClass.api.VerifyRequest
import com.example.base44.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class signUp : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etUsername: EditText
    private lateinit var btnSignup: Button
    private lateinit var tvLoginIn: TextView
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        session = SessionManager(this)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        etUsername = findViewById(R.id.etUserName)
        btnSignup = findViewById(R.id.btnSignup)
        tvLoginIn = findViewById(R.id.tvLoginIn)
    }

    private fun setupListeners() {
        btnSignup.setOnClickListener { 
            attemptSignup() 
        }

        tvLoginIn.setOnClickListener {
            startActivity(Intent(this, login::class.java))
            finish()
        }
    }

    private fun attemptSignup() {
        val email = etEmail.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        btnSignup.isEnabled = false
        Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show()

        val request = RegisterRequest(username, email, password)

        RetrofitClient.instance.register(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                btnSignup.isEnabled = true

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.status == "success" || response.code() == 200 || response.code() == 201) {
                         // Signup Success -> Direct Login (Bypassing Verification)
                         val user = authResponse?.user
                         val token = authResponse?.token
                         
                         session.saveLogin(
                            role = user?.role ?: "user",
                            token = token,
                            username = user?.fullName ?: user?.username ?: "User",
                            email = user?.email
                        )
                        Toast.makeText(this@signUp, "Welcome!", Toast.LENGTH_SHORT).show()
                        
                        val intent = Intent(this@signUp, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        showErrorDialog("Registration Failed", response.code().toString(), authResponse?.message ?: "Unknown Error")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown Error"
                    showErrorDialog("Request Failed", response.code().toString(), errorBody)
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                btnSignup.isEnabled = true
                showErrorDialog("Network Error", "0", t.message ?: "Connection failed")
            }
        })
    }

    private fun showErrorDialog(title: String, code: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("Code: $code\nMessage: $message")
            .setPositiveButton("OK", null)
            .show()
    }
}
