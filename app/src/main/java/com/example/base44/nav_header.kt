package com.example.base44

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class nav_header : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var username_tv : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.nav_header)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        username_tv = findViewById(R.id.tvUsername)

//        val currentUser = auth.currentUser
//        currentUser?.uid?.let { uid ->
//            db.collection("users").document(uid).get()
//                .addOnSuccessListener { document ->
//                    if (document != null && document.exists()) {
//                        val username = document.getString("username")
//                        username_tv.text = username ?: "User"
//                    }
//                }
//                .addOnFailureListener { e ->
//                    username_tv.text = "User"
//                }
//        }



    }
}