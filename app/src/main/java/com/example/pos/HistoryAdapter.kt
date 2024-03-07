package com.example.pos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private var historyList: List<HistoryData>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.each_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyData = historyList[position]
        holder.bind(historyData)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    fun updateData(newList: List<HistoryData>) {
        historyList = newList
        notifyDataSetChanged()
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderNumberTextView: TextView = itemView.findViewById(R.id.order_number)
        private val staffNameTextView: TextView = itemView.findViewById(R.id.staff_name)
        private val dateTimeTextView: TextView = itemView.findViewById(R.id.date_time)
        private val totalTextView: TextView = itemView.findViewById(R.id.total)
        private val productQuantityTextView: TextView = itemView.findViewById(R.id.product_quantity)

        fun bind(historyData: HistoryData) {
            orderNumberTextView.text = historyData.orderNumber
            staffNameTextView.text = historyData.staffName
            dateTimeTextView.text = historyData.orderDateTime
            totalTextView.text = historyData.total.toString()

            // Calculate total quantity of products
            var totalQuantity = 0
            for ((_, quantity) in historyData.orderItems) {
                totalQuantity += quantity
            }
            productQuantityTextView.text = totalQuantity.toString()
        }
    }
}