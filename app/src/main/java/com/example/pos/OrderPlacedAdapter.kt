package com.example.pos

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class OrderPlacedAdapter(private var orderList: List<OrderData>, private val databaseReference: DatabaseReference) : RecyclerView.Adapter<OrderPlacedAdapter.OrderPlacedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderPlacedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.confirm_orders, parent, false)
        return OrderPlacedViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderPlacedViewHolder, position: Int) {
        val orderData = orderList[position]
        holder.bind(orderData)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    fun updateData(newList: List<OrderData>) {
        orderList = newList
        notifyDataSetChanged()
    }

    inner class OrderPlacedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderNumberTextView: TextView = itemView.findViewById(R.id.order_number)
        private val customerNameTextView: TextView = itemView.findViewById(R.id.customer_name)
        private val staffNameTextView: TextView = itemView.findViewById(R.id.staff_name)
        private val preferenceTextView: TextView = itemView.findViewById(R.id.preference)
        private val subtotalTextView: TextView = itemView.findViewById(R.id.subtotal)
        private val discountTextView: TextView = itemView.findViewById(R.id.discount)
        private val totalTextView: TextView = itemView.findViewById(R.id.all_total)
        private val dateTimeTextView: TextView = itemView.findViewById(R.id.date_time)
        private val orderListRecyclerView: RecyclerView = itemView.findViewById(R.id.order_lists)

        fun bind(orderData: OrderData) {
            orderNumberTextView.text = orderData.orderNumber // Set order number
            customerNameTextView.text = orderData.CustomerName
            staffNameTextView.text = orderData.StaffName
            preferenceTextView.text = orderData.Preference
            subtotalTextView.text = orderData.Subtotal.toString()
            discountTextView.text = orderData.Discount.toString()
            totalTextView.text = orderData.Total.toString()
            dateTimeTextView.text = orderData.OrderDateTime // Set order date time

            val orderItems = orderData.orderItems?.values?.toList() ?: emptyList() // Access values from map

            // Set up inner RecyclerView with a linear layout
            val orderListAdapter = OrderListAdapter(orderItems)
            orderListRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            orderListRecyclerView.adapter = orderListAdapter

            itemView.findViewById<View>(R.id.done_button).setOnClickListener {
                moveOrderToHistory(orderData.orderNumber ?: "")
                showOrderCompleteToast(itemView.context)
            }
        }

        private fun moveOrderToHistory(orderNumber: String) {
            val historyReference = FirebaseDatabase.getInstance().getReference("history")
            val ordersReference = FirebaseDatabase.getInstance().getReference("orders")

            val orderData = orderList.find { it.orderNumber == orderNumber }
            orderData?.let {
                val orderKey = historyReference.push().key
                orderKey?.let { key ->
                    historyReference.child(key).setValue(it)
                }

                ordersReference.child(orderNumber).removeValue()
            }
        }

        private fun showOrderCompleteToast(context: Context) {
            Toast.makeText(context, "Order Complete", Toast.LENGTH_SHORT).show()
        }
    }
}