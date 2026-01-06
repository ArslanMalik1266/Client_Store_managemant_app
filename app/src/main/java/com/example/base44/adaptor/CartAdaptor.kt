package com.example.base44.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.dataClass.CartRow
import com.example.base44.dataClass.add_to_cart_item

class CartAdapter(
    private val cartList: List<add_to_cart_item>,
    private val onDeleteClick: ((position: Int) -> Unit)? = null // optional
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val cartItems = cartList.toMutableList()

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.cart_item_name)
        val productCode: TextView = itemView.findViewById(R.id.cart_item_code)
        val invoiceNumber: TextView = itemView.findViewById(R.id.inv_number_tv)
        val dateAdded: TextView = itemView.findViewById(R.id.cart_item_date_added)
        val rvRows: RecyclerView = itemView.findViewById(R.id.recyclerview_cartsizes)
        val deleteBtn: ImageView = itemView.findViewById(R.id.cartDelete_item)
        val cartImage: ImageView = itemView.findViewById(R.id.cartImage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_item_view, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartList[position]

        holder.productName.text = item.productName
        holder.productCode.text = item.productCode
        holder.invoiceNumber.text = item.tempInvoice?.let { "INV No: $it" } ?: "INV No: ---"
        holder.cartImage.setImageResource(item.imageRes)
        val dateText = if (item.raceDays.isNotEmpty()) {
            item.raceDays.last()
        } else {
            val sdf = java.text.SimpleDateFormat("EEE dd/MM/yyyy", java.util.Locale.getDefault())
            sdf.format(java.util.Date())
        }
        holder.dateAdded.text = dateText
        holder.rvRows.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.rvRows.adapter = TagAdapter(item.rows)


        onDeleteClick?.let { callback ->
            holder.deleteBtn.setOnClickListener { callback(position) }
        }
    }

    override fun getItemCount(): Int = cartList.size

    fun updateData(newList: List<add_to_cart_item>) {
        cartItems.clear()
        cartItems.addAll(newList)
        notifyDataSetChanged()
    }
    fun addItemAtTop(item: add_to_cart_item, recyclerView: RecyclerView) {
        cartItems.add(0, item)       // add to top
        notifyItemInserted(0)         // notify adapter
        recyclerView.scrollToPosition(0) // scroll to top
    }

}
