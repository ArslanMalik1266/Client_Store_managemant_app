package com.example.base44.adaptor.utils

import android.content.Context


class SessionManager(context: android.content.Context) {
    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveLogin(role: String, token: String? = null, username: String? = null, email: String? = null, userId: String? = null) {
        prefs.edit()
            .putBoolean("isLoggedIn", true)
            .putString("role", role)
            .putString("token", token)
            .putString("username", username)
            .putString("email", email)
            .putString("user_id", userId)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }
    fun getRole(): String? {
        return prefs.getString("role", null)
    }

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    fun getUsername(): String? {
        return prefs.getString("username", null)
    }

    fun getEmail(): String? {
        return prefs.getString("email", null)
    }

    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}