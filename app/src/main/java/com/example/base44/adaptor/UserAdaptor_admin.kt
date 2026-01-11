package com.example.base44.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.dataClass.User_for_admin

class UserAdapte_admin(
    private var users: List<User_for_admin>,
    private val onEditCreditsClick: (User_for_admin) -> Unit,
    private val onItemClick: (User_for_admin) -> Unit
) : RecyclerView.Adapter<UserAdapte_admin.UserViewHolder>() {

    fun updateList(newList: List<User_for_admin>) {
        this.users = newList
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val tvInitial: TextView = itemView.findViewById(R.id.tvUserInitial)
        val tvBalance: TextView = itemView.findViewById(R.id.itemTvBalance)
        val tvLimit: TextView = itemView.findViewById(R.id.itemTvWalletLimit)
        val tvSales: TextView = itemView.findViewById(R.id.itemTvTotalSales)
        val tvStatusTag: TextView = itemView.findViewById(R.id.tvStatusTag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_users, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvName.text = user.fullName
        holder.tvEmail.text = user.email
        holder.tvInitial.text = user.fullName.firstOrNull()?.uppercase() ?: "U"
        holder.tvBalance.text = "RM %.2f".format(user.currentBalance)
        holder.tvLimit.text = "RM %.2f".format(user.creditLimit)
        holder.tvSales.text = "RM %.2f".format(user.totalSales)

        holder.tvStatusTag.visibility = if (user.canWork) View.GONE else View.VISIBLE

        holder.itemView.setOnClickListener { onItemClick(user) }
    }

    override fun getItemCount(): Int = users.size
}
