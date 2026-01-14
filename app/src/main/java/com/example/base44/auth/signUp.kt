package com.example.base44.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.base44.MainActivity
import com.example.base44.R
import com.example.base44.adaptor.utils.SessionManager
import com.example.base44.dataClass.api.*
import com.example.base44.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUp : AppCompatActivity() {

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
        btnSignup.setOnClickListener { attemptSignup() }
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

        // Validation
        when {
            email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show()
                return
            }
            password != confirmPassword -> {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return
            }
        }

        btnSignup.isEnabled = false
        
        // Call API
        val request = RegisterRequest(username, email, password)
        RetrofitClient.instance.register(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                btnSignup.isEnabled = true
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@SignUp, "Account created! Verify your email.", Toast.LENGTH_SHORT).show()
                    showOTPDialog(email, response.body()?.token, username)
                } else {
                    // Account might exist, try resend
                    resendAndShowDialog(email, username)
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                btnSignup.isEnabled = true
                Toast.makeText(this@SignUp, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resendAndShowDialog(email: String, username: String) {
        RetrofitClient.instance.resendOtp(ResendOtpRequest(email)).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                Toast.makeText(this@SignUp, "Code sent to email", Toast.LENGTH_SHORT).show()
                showOTPDialog(email, null, username)
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@SignUp, "Failed to send code", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showOTPDialog(email: String, token: String?, username: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_otp_verification, null)
        val dialog = AlertDialog.Builder(this).setView(view).setCancelable(false).create()

        val etCode = view.findViewById<EditText>(R.id.etDialogCode)
        val btnVerify = view.findViewById<Button>(R.id.btnDialogVerify)
        val tvResend = view.findViewById<TextView>(R.id.tvDialogResend)
        val progress = view.findViewById<ProgressBar>(R.id.dialogProgressBar)

        view.findViewById<TextView>(R.id.tvDialogSubtitle).text = "Enter code sent to $email"

        btnVerify.setOnClickListener {
            val code = etCode.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyOTP(email, code, token, username, dialog, progress, btnVerify)
        }

        tvResend.setOnClickListener { resendAndShowDialog(email, username) }
        dialog.show()
    }

    private fun verifyOTP(email: String, code: String, token: String?, username: String, 
                         dialog: AlertDialog, progress: ProgressBar, btnVerify: Button) {
        btnVerify.isEnabled = false
        progress.visibility = View.VISIBLE

        RetrofitClient.instance.verifyEmail(VerifyRequest(email, code))
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    progress.visibility = View.GONE
                    btnVerify.isEnabled = true

                    if (response.isSuccessful && response.body()?.status == "success") {
                        val body = response.body()
                        session.saveLogin(
                            role = body?.user?.role ?: "user",
                            token = body?.token ?: token,
                            username = body?.user?.fullName ?: username,
                            email = email
                        )
                        
                        Toast.makeText(this@SignUp, "Success!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        
                        startActivity(Intent(this@SignUp, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                        finish()
                    } else {
                        // Show actual error from API
                        val errorMsg = response.body()?.message 
                            ?: response.errorBody()?.string() 
                            ?: "Verification failed"
                        Toast.makeText(this@SignUp, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    progress.visibility = View.GONE
                    btnVerify.isEnabled = true
                    Toast.makeText(this@SignUp, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
