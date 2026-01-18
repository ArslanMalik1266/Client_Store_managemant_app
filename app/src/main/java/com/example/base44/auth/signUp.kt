package com.example.base44.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        etUsername = findViewById(R.id.etUserName)
        btnSignup = findViewById(R.id.btnSignup)
        tvLoginIn = findViewById(R.id.tvLoginIn)

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

        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        btnSignup.isEnabled = false
        val request = RegisterRequest(username, email, password)

        RetrofitClient.instance.register(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                btnSignup.isEnabled = true
                val body = response.body()

                if (response.isSuccessful && (body?.status == "success" || response.code() in 200..201)) {
                    Toast.makeText(this@SignUp, "Verify your email.", Toast.LENGTH_SHORT).show()
                    showOTPDialog(email, body?.token, username)
                }
                else if (body?.message?.contains("already exists") == true) {
                    Toast.makeText(this@SignUp, "User exists! Enter OTP sent to email.", Toast.LENGTH_SHORT).show()
                    showOTPDialog(email, null, username)
                } else {
                    Toast.makeText(this@SignUp, body?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                btnSignup.isEnabled = true
                Toast.makeText(this@SignUp, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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

        tvResend.setOnClickListener {
            resendOTP(email)
        }

        dialog.show()
    }

    private fun resendOTP(email: String) {
        RetrofitClient.instance.resendOtp(ResendOtpRequest(email)).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                Toast.makeText(this@SignUp, "Code resent to email", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@SignUp, "Failed to resend code", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun verifyOTP(email: String, code: String, token: String?, username: String,
                          dialog: AlertDialog, progress: ProgressBar, btnVerify: Button) {
        btnVerify.isEnabled = false
        progress.visibility = android.view.View.VISIBLE

        RetrofitClient.instance.verifyEmail(VerifyRequest(email, code))
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    progress.visibility = android.view.View.GONE
                    btnVerify.isEnabled = true

                    val body = response.body()
                    if (response.isSuccessful && (body?.status == "success" || response.code() == 200)) {
                        Toast.makeText(this@SignUp, "Verified! Please login.", Toast.LENGTH_SHORT).show()
                        if (dialog.isShowing) dialog.dismiss()

                        startActivity(Intent(this@SignUp, login::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                        finish()
                    } else {
                        Toast.makeText(
                            this@SignUp,
                            body?.message ?: response.errorBody()?.string() ?: "Verification failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    progress.visibility = android.view.View.GONE
                    btnVerify.isEnabled = true
                    Toast.makeText(this@SignUp, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
