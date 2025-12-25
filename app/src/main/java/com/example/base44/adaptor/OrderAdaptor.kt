package com.example.base44.adaptor


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.dataClass.OrderItem

class OrdersAdapter(
    private val orderList: List<OrderItem>,
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
        val hashtagTv: TextView = itemView.findViewById(R.id.hashtag_code_tv)
        val rmTv: TextView = itemView.findViewById(R.id.rm_code_tv)
        val totalLabel: TextView = itemView.findViewById(R.id.total_tv)
        val totalAmount: TextView = itemView.findViewById(R.id.total_amount)

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
        holder.productImage.setImageResource(item.productImage)
        holder.productName.text = item.productName
        holder.productCode.text = item.productCode
        holder.hashtagTv.text = item.hashtag
        holder.rmTv.text = item.rmAmount
        holder.totalLabel.text = item.totalLabel
        holder.totalAmount.text = item.totalAmount
    }

    override fun getItemCount(): Int = orderList.size
}
