package com.example.base44.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.base44.R
import com.example.base44.dataClass.ResultItem

class ResultAdapter :
    ListAdapter<ResultItem, ResultAdapter.ResultViewHolder>(ResultDiffCallback()) {

    inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.result_ItemHeading)
        private val date: TextView = itemView.findViewById(R.id.date_resultItem)

        private val firstPrizeNumber: TextView = itemView.findViewById(R.id.result_1stPrize_number)
        private val secondPrize: TextView = itemView.findViewById(R.id.result_2nd_number)
        private val thirdPrize: TextView = itemView.findViewById(R.id.result_3nd_number)

        private val specialIds = listOf(
            R.id.special_nmbr_1, R.id.special_nmbr_2, R.id.special_nmbr_3,
            R.id.special_nmbr_4, R.id.special_nmbr_5, R.id.special_nmbr_6,
            R.id.special_nmbr_7, R.id.special_nmbr_8, R.id.special_nmbr_9,
            R.id.special_nmbr_10
        )

        private val consolationIds = listOf(
            R.id.consolation_number_1, R.id.consolation_number_2,
            R.id.consolation_number_3, R.id.consolation_number_4,
            R.id.consolation_number_5, R.id.consolation_number_6,
            R.id.consolation_number_7, R.id.consolation_number_8,
            R.id.consolation_number_9, R.id.consolation_number_10
        )

        private val specialViews = specialIds.map { itemView.findViewById<TextView>(it) }
        private val consolationViews = consolationIds.map { itemView.findViewById<TextView>(it) }

        fun bind(item: ResultItem) {
            title.text = item.title
            date.text = item.date
            firstPrizeNumber.text = item.firstPrize
            secondPrize.text = item.secondPrize
            thirdPrize.text = item.thirdPrize

            item.specialNumbers.forEachIndexed { index, number ->
                if (index < specialViews.size) specialViews[index].text = number
            }

            item.consolationNumbers.forEachIndexed { index, number ->
                if (index < consolationViews.size) consolationViews[index].text = number
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_draw_result, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateData(newList: List<ResultItem>) {
        submitList(newList)
    }
}

class ResultDiffCallback : DiffUtil.ItemCallback<ResultItem>() {
    override fun areItemsTheSame(oldItem: ResultItem, newItem: ResultItem): Boolean {
        return oldItem.date == newItem.date && oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: ResultItem, newItem: ResultItem): Boolean {
        return oldItem == newItem
    }
}
