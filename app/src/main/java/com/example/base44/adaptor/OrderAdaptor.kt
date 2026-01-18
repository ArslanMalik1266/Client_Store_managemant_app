package com.example.base44.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.base44.R
import com.example.base44.dataClass.OrderItem

class OrdersAdapter(
    private val onItemClick: ((position: Int) -> Unit)? = null
) : ListAdapter<OrderItem, OrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val invoiceTv: TextView = itemView.findViewById(R.id.inv_number_tv)
        val statusTv: TextView = itemView.findViewById(R.id.completed_tv)
        val dateTv: TextView = itemView.findViewById(R.id.cart_item_date_added)
        val raceDayTv: TextView = itemView.findViewById(R.id.race_day_tv)
        val productImage: ImageView = itemView.findViewById(R.id.cartImage)
        val productName: TextView = itemView.findViewById(R.id.cart_item_name)
        val productCode: TextView = itemView.findViewById(R.id.cart_item_code)
        val totalLabel: TextView = itemView.findViewById(R.id.total_tv)
        val totalAmount: TextView = itemView.findViewById(R.id.total_amount)
        val rvRows: RecyclerView = itemView.findViewById(R.id.recyclerview_orderSizes)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(adapterPosition)
            }
        }

        fun bind(item: OrderItem) {
            invoiceTv.text = item.invoiceNumber
            statusTv.text = item.status
            dateTv.text = item.dateAdded
            raceDayTv.text = "Race day: ${item.raceDay}"
            productName.text = item.productName
            productCode.text = item.productCode
            totalLabel.text = item.totalLabel
            totalAmount.text = item.totalAmount

            Glide.with(itemView.context)
                .load(item.productImage)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(productImage)

            rvRows.layoutManager = LinearLayoutManager(itemView.context)
            rvRows.adapter = TagAdapter(item.rows ?: emptyList())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.orders_item_view, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<OrderItem>() {
        override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem) =
            oldItem.invoiceNumber == newItem.invoiceNumber

        override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem) =
            oldItem == newItem
    }
}
