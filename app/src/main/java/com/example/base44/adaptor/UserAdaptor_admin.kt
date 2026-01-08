package com.example.base44.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.dataClass.User_for_admin

class UserAdapte_admin(
    private val users: List<User_for_admin>,
    private val onEditCreditsClick: (User_for_admin) -> Unit,
    private val onItemClick: (User_for_admin) -> Unit
) : RecyclerView.Adapter<UserAdapte_admin.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val btnEdit: TextView = itemView.findViewById(R.id.btnEditCredits)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_users, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvName.text = user.fullName
        holder.tvEmail.text = user.email

        holder.itemView.setOnClickListener { onItemClick(user) }
        holder.btnEdit.setOnClickListener { onEditCreditsClick(user) }
    }

    override fun getItemCount(): Int = users.size
}
