package com.example.base44.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.dataClass.SimpleOrderItem

// ✅ Using ListAdapter + DiffUtil for smooth updates with large data
class SimpleOrdersAdapter(
    private val onItemClick: ((position: Int) -> Unit)? = null
) : ListAdapter<SimpleOrderItem, SimpleOrdersAdapter.OrderViewHolder>(DiffCallback()) {

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

        fun bind(item: SimpleOrderItem) {
            invoiceTv.text = item.invoiceNumber
            dateTv.text = item.dateAdded
            totalAmountTv.text = item.totalAmount
            statusTv.text = item.status
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.wallet_history_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateData(newList: List<SimpleOrderItem>) {
        submitList(newList)
    }

    class DiffCallback : DiffUtil.ItemCallback<SimpleOrderItem>() {
        override fun areItemsTheSame(oldItem: SimpleOrderItem, newItem: SimpleOrderItem): Boolean {
            return oldItem.invoiceNumber == newItem.invoiceNumber // unique identifier
        }

        override fun areContentsTheSame(oldItem: SimpleOrderItem, newItem: SimpleOrderItem): Boolean {
            return oldItem == newItem
        }
    }
}
