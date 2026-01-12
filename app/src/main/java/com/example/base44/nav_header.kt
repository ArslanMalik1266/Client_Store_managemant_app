package com.example.base44

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class nav_header : AppCompatActivity() {

    private lateinit var username_tv: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.nav_header)

        initView()
    }

    private fun initView() {
        username_tv = findViewById(R.id.tvUsername)
    }
}