package com.example.pos

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

interface OnOrderChangedListener {
    fun onOrderChanged(orderItemList: List<OrderItem>)
}

class OrderAdapter(
    private val context: Context,
    private var orderItemList: MutableList<OrderItem>,
    private val listener: OnOrderChangedListener,
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.order_list_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orderItem = orderItemList[position]

        holder.textProductName.text = orderItem.productName
        holder.textPrice.text = "P${orderItem.price}" // Display price with 'P'
        holder.textSize.text = orderItem.size
        holder.textTemp.text = orderItem.temperature
        holder.textQuantity.text = orderItem.quantity.toString()

        holder.addButton.setOnClickListener {
            // Increment quantity
            orderItem.quantity++
            holder.textQuantity.text = orderItem.quantity.toString()
            holder.textPrice.text = "P${orderItem.price * orderItem.quantity}" // Update price after increment
            listener.onOrderChanged(orderItemList)
        }

        holder.minusButton.setOnClickListener {
            // Decrement quantity if greater than 0
            if (orderItem.quantity > 0) {
                orderItem.quantity--
                holder.textQuantity.text = orderItem.quantity.toString()
                holder.textPrice.text = "P${orderItem.price * orderItem.quantity}" // Update price after decrement
                listener.onOrderChanged(orderItemList)
            }
            // Check if the quantity is zero, mark the item as deleted
            if (orderItem.quantity == 0) {
                orderItemList.removeAt(position)
                orderItem.deleted = true
                notifyDataSetChanged() // Update the adapter
                listener.onOrderChanged(orderItemList)
            }
        }
    }

    override fun getItemCount(): Int {
        return orderItemList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textProductName: TextView = itemView.findViewById(R.id.text_product_name)
        val textPrice: TextView = itemView.findViewById(R.id.text_prices)
        val textSize: TextView = itemView.findViewById(R.id.text_product_size)
        val textQuantity: TextView = itemView.findViewById(R.id.product_quantity)
        val textTemp : TextView = itemView.findViewById(R.id.text_product_temp)
        val addButton: Button = itemView.findViewById(R.id.add_button)
        val minusButton: Button = itemView.findViewById(R.id.minus_button)
    }

    fun updateOrderItemList(newOrderItemList: List<OrderItem>) {
        // Filter out the deleted items
        orderItemList = newOrderItemList.filter { !it.deleted }.toMutableList()
        notifyDataSetChanged()
    }
}
