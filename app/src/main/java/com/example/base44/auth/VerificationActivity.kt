package com.example.base44.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.base44.MainActivity
import com.example.base44.R
import com.example.base44.adaptor.utils.SessionManager
import com.example.base44.dataClass.api.AuthResponse
import com.example.base44.dataClass.api.VerifyRequest
import com.example.base44.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerificationActivity : AppCompatActivity() {

    private lateinit var etCode: EditText
    private lateinit var btnVerify: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSubtitle: TextView
    private lateinit var session: SessionManager
    
    // Data passed from SignUp
    private var userEmail: String? = null
    private var userToken: String? = null
    private var userFullName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        session = SessionManager(this)

        userEmail = intent.getStringExtra("EMAIL")
        userToken = intent.getStringExtra("TOKEN")
        userFullName = intent.getStringExtra("NAME")

        initViews()

        tvSubtitle.text = "Please enter the code sent to ${userEmail ?: "your email"}"

        btnVerify.setOnClickListener {
            val code = etCode.text.toString().trim()
            if (code.isNotEmpty()) {
                verifyCode(code)
            } else {
                Toast.makeText(this, "Please enter the code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViews() {
        etCode = findViewById(R.id.etCode)
        btnVerify = findViewById(R.id.btnVerify)
        progressBar = findViewById(R.id.progressBar)
        tvSubtitle = findViewById(R.id.tvSubtitle)
    }

    private fun verifyCode(code: String) {
        val email = userEmail ?: return
        
        // Ensure token is set in Retrofit (Critical for some APIs)
        if (userToken != null) {
            RetrofitClient.setAuthToken(userToken)
        }

        val request = VerifyRequest(email, code)
        
        setLoading(true)

        // Try 'verify-email' endpoint first (User reported previous one was verify)
        // Adjust ApiService if needed to match what likely works
        RetrofitClient.instance.verifyEmail(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                setLoading(false)
                
                if (response.isSuccessful) {
                    val auth = response.body()
                    if (auth?.status == "success" || response.code() == 200) {
                        // Success
                        session.saveLogin(
                            role = auth?.user?.role ?: "user",
                            token = userToken ?: auth?.token,
                            username = auth?.user?.fullName ?: userFullName,
                            email = email
                        )
                        Toast.makeText(this@VerificationActivity, "Verified Successfully!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@VerificationActivity, MainActivity::class.java))
                        finishAffinity()
                    } else {
                        showError("Verification Failed", auth?.message ?: "Unknown Error")
                    }
                } else {
                    val rawError = response.errorBody()?.string() ?: "No error body"
                    showError("Request Failed (${response.code()})", rawError)
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                setLoading(false)
                showError("Network Error", t.message ?: "Connection failed")
            }
        })
    }

    private fun setLoading(loading: Boolean) {
        btnVerify.isEnabled = !loading
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnVerify.text = if (loading) "Verifying..." else "Verify Now"
    }

    private fun showError(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
