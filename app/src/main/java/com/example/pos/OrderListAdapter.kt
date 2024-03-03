package com.example.pos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderListAdapter(private val orderList: List<OrderList>) : RecyclerView.Adapter<OrderListAdapter.OrderListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_list, parent, false)
        return OrderListViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderListViewHolder, position: Int) {
        val orderItem = orderList[position]
        holder.bind(orderItem)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    inner class OrderListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productNameTextView: TextView = itemView.findViewById(R.id.text_product_name)
        private val productQuantityTextView: TextView = itemView.findViewById(R.id.product_quantity)
        private val productSizeTextView: TextView = itemView.findViewById(R.id.text_product_size)
        private val productPriceTextView: TextView = itemView.findViewById(R.id.text_prices)
        private val productTotalTextView: TextView = itemView.findViewById(R.id.total)

        fun bind(orderItem: OrderList) {
            productNameTextView.text = orderItem.ProductName
            productQuantityTextView.text = orderItem.Quantity.toString()
            productSizeTextView.text = orderItem.Size
            productPriceTextView.text = orderItem.Price.toString()
            productTotalTextView.text = (orderItem.Price!! * orderItem.Quantity!!).toString()
        }
    }
}
