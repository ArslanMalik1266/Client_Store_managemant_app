package com.example.base44.adaptor

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.dataClass.DrawResult

class ResultAdapter(private val results: List<DrawResult>) :
    RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {

    class ResultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDrawDate: TextView = view.findViewById(R.id.tvDrawDate)
        val tvWinningNumber: TextView = view.findViewById(R.id.tvWinningNumber)
        val layoutRows: LinearLayout = view.findViewById(R.id.layoutRows)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_draw_result, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val item = results[position]

        // Draw Date
        holder.tvDrawDate.text = item.draw_date

        // Winning number highlight
        holder.tvWinningNumber.text = "Winning #: ${item.winning_number}"

        // Dynamic rows
        holder.layoutRows.removeAllViews()

        item.rows.forEach { row ->

            // Create row box dynamically
            val rowView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_draw_row, holder.layoutRows, false)

            val tvNumber = rowView.findViewById<TextView>(R.id.tvRowNumber)

            tvNumber.text = row.number

            // Highlight if this row is winner
            if (row.number == item.winning_number) {
                tvNumber.setBackgroundResource(R.drawable.bg_winner)
                tvNumber.setTextColor(Color.WHITE)
            } else {
                tvNumber.setBackgroundResource(R.drawable.bg_row)
                tvNumber.setTextColor(Color.BLACK)
            }

            holder.layoutRows.addView(rowView)
        }
    }

    override fun getItemCount() = results.size
}
