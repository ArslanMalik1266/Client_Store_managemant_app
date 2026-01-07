package com.example.base44.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.dataClass.SimpleOrderItem

class SimpleOrdersAdapter(
    private var orderList: List<SimpleOrderItem>,
    private val onItemClick: ((position: Int) -> Unit)? = null
) : RecyclerView.Adapter<SimpleOrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val invoiceTv: TextView = itemView.findViewById(R.id.inv_number_tv)
        val dateTv: TextView = itemView.findViewById(R.id.cart_item_date_added)
        val totalAmountTv: TextView = itemView.findViewById(R.id.total_amount)
        val statusTv: TextView = itemView.findViewById(R.id.completed_tv)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.wallet_history_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val item = orderList[position]
        holder.invoiceTv.text = item.invoiceNumber
        holder.dateTv.text = item.dateAdded
        holder.totalAmountTv.text = item.totalAmount
        holder.statusTv.text = item.status
    }

    override fun getItemCount(): Int = orderList.size

    fun updateData(newList: List<SimpleOrderItem>) {
        orderList = newList
        notifyDataSetChanged()
    }
}
