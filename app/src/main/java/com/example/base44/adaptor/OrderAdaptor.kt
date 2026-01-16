package com.example.base44.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.base44.R
import com.example.base44.dataClass.OrderItem

class OrdersAdapter(
    private var orderList: List<OrderItem>,
    private val onItemClick: ((position: Int) -> Unit)? = null
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.orders_item_view, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val item = orderList[position]

        holder.invoiceTv.text = item.invoiceNumber
        holder.statusTv.text = item.status
        holder.dateTv.text = item.dateAdded
        holder.raceDayTv.text = "Race day: ${item.raceDay}"

        if (item.productImage.startsWith("http")) {
            Glide.with(holder.itemView.context)
                .load(item.productImage)
                .into(holder.productImage)
        } else {
            val resId = holder.itemView.context.resources.getIdentifier(
                item.productImage, "drawable", holder.itemView.context.packageName
            )
            if (resId != 0) holder.productImage.setImageResource(resId)
        }

        holder.productName.text = item.productName
        holder.productCode.text = item.productCode
        holder.totalLabel.text = item.totalLabel
        holder.totalAmount.text = item.totalAmount

        // RecyclerView for rows inside each order
        holder.rvRows.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.rvRows.adapter = TagAdapter(item.rows ?: emptyList())
    }

    override fun getItemCount(): Int = orderList.size

    fun updateData(newList: List<OrderItem>) {
        this.orderList = newList
        notifyDataSetChanged()
    }
}
