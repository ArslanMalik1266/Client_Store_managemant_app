package com.example.base44.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.dataClass.CartItem

class CartAdapter(
    private val cartList: List<CartItem>,
    private val onDeleteClick: (position: Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.cartImage)
        val productName: TextView = itemView.findViewById(R.id.cart_item_name)
        val productCode: TextView = itemView.findViewById(R.id.cart_item_code)
        val invoiceNumber: TextView = itemView.findViewById(R.id.inv_number_tv)
        val dateAdded: TextView = itemView.findViewById(R.id.cart_item_date_added)
        val hashtagCode: TextView = itemView.findViewById(R.id.hashtag_code_tv)
        val rmCode: TextView = itemView.findViewById(R.id.rm_code_tv)
        val deleteBtn: ImageView = itemView.findViewById(R.id.cartDelete_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_item_view, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartList[position]

        holder.productImage.setImageResource(item.imageRes)
        holder.productName.text = item.name
        holder.productCode.text = item.code
        holder.invoiceNumber.text = item.invoiceNumber
        holder.dateAdded.text = item.dateAdded
        holder.hashtagCode.text = item.hashtagCode
        holder.rmCode.text = item.rmCode

        holder.deleteBtn.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = cartList.size
}
