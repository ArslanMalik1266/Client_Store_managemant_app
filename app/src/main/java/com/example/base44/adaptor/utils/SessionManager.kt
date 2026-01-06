package com.example.base44.adaptor.utils

import android.content.Context


class SessionManager(context: android.content.Context) {
    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveLogin(role: String) {
        prefs.edit()
            .putBoolean("isLoggedIn", true)
            .putString("role", role)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }
    fun getRole(): String? {
        return prefs.getString("role", null)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}