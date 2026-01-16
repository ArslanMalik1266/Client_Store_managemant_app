package com.example.base44.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.dataClass.CartRow

class TagAdapter(
    private val list: List<CartRow>
) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cartItemNumber: TextView = itemView.findViewById(R.id.hashtag_code_tv)
        val cartRm : TextView = itemView.findViewById(R.id.rm_code_tv)
        val cardB: CardView = itemView.findViewById(R.id.cardview_b)
        val cardS: CardView = itemView.findViewById(R.id.cardview_s)
        val cardA: CardView = itemView.findViewById(R.id.cardview_a)
        val cardIB: CardView = itemView.findViewById(R.id.cardview_ib)
        val cardBX: CardView = itemView.findViewById(R.id.cardview_bx)
        val cardBXA: CardView = itemView.findViewById(R.id.cardview_bxa)
        val cardBXS: CardView = itemView.findViewById(R.id.cardview_bxs)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_sizes_rv, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val item = list[position]

        holder.cartItemNumber.text = item.number ?: "-"
        holder.cartRm.text = item.amount
        holder.cardB.visibility = View.GONE
        holder.cardS.visibility = View.GONE
        holder.cardA.visibility = View.GONE
        holder.cardIB.visibility = View.GONE
        holder.cardBX.visibility = View.GONE
        holder.cardBXA.visibility = View.GONE
        holder.cardBXS.visibility = View.GONE

        item.selectedCategories.forEach { category ->
            when (category) {
                "B" -> holder.cardB.visibility = View.VISIBLE
                "X" -> holder.cardS.visibility = View.VISIBLE
                "A" -> holder.cardA.visibility = View.VISIBLE
                "IB" -> holder.cardIB.visibility = View.VISIBLE
                "BX" -> holder.cardBX.visibility = View.VISIBLE
                "BXA" -> holder.cardBXA.visibility = View.VISIBLE
                "BXS" -> holder.cardBXS.visibility = View.VISIBLE
            }
        }


    }

    override fun getItemCount(): Int = list.size
}
