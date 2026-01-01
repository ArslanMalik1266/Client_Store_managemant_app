package com.example.base44.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.dataClass.TagItem

class TagAdapter(
    private val list: List<TagItem>
) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.hashtag_code_tv)
        val cardView: CardView = itemView.findViewById(R.id.cardView2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_sizes_rv, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val item = list[position]

        holder.textView.text = item.text


    }

    override fun getItemCount(): Int = list.size
}
