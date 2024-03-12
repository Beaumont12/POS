package com.example.pos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class Order : Fragment() {

    private lateinit var orderPlacedAdapter: OrderPlacedAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var orderList: List<OrderData>
    private lateinit var searchBar: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order, container, false)

        recyclerView = view.findViewById(R.id.recycleViewOrder)
        searchBar = view.findViewById(R.id.search_bar)
        fetchOrderData()

        val dineInButton: MaterialButton = view.findViewById(R.id.dinein_button)
        val takeOutButton: MaterialButton = view.findViewById(R.id.takeout_button)

        dineInButton.setOnClickListener {
            filterOrdersByPreference("Dine In")
        }

        takeOutButton.setOnClickListener {
            filterOrdersByPreference("Take Out")
        }

        // Initialize search bar
        val searchBar = view.findViewById<EditText>(R.id.search_bar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter the orders based on the entered text
                val searchText = s.toString().trim().toLowerCase(Locale.getDefault())
                val filteredOrders = if (searchText.isEmpty()) {
                    orderList // Show all orders if search text is empty
                } else {
                    orderList.filter { order ->
                        order.CustomerName?.toLowerCase(Locale.getDefault())?.contains(searchText) ?: false ||
                                order.orderNumber?.toLowerCase(Locale.getDefault())?.contains(searchText) ?: false
                    }
                }
                orderPlacedAdapter.updateData(filteredOrders)
            }

            override fun afterTextChanged(s: Editable?) {
                // No implementation needed
            }
        })

        return view
    }

    private fun fetchOrderData() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("orders")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val orderList = mutableListOf<OrderData>()

                for (orderSnapshot in dataSnapshot.children) {
                    val orderNumber = orderSnapshot.key
                    val orderData = orderSnapshot.getValue(OrderData::class.java)

                    orderData?.let { data ->
                        data.orderNumber = orderNumber
                        val orderListData = mutableMapOf<String, OrderList>()
                        orderSnapshot.children.forEach { orderItemSnapshot ->
                            val key = orderItemSnapshot.key
                            if (key != null && key.startsWith("Order_")) {
                                val price = orderItemSnapshot.child("Price").getValue(Double::class.java) ?: 0.0
                                val productName = orderItemSnapshot.child("ProductName").getValue(String::class.java) ?: ""
                                val quantity = orderItemSnapshot.child("Quantity").getValue(Int::class.java) ?: 0
                                val size = orderItemSnapshot.child("Size").getValue(String::class.java) ?: ""
                                val orderItem = OrderList(Price = price, ProductName = productName, Quantity = quantity, Size = size)
                                orderListData[key] = orderItem
                            }
                        }
                        data.orderItems = orderListData
                        orderList.add(data)
                    }
                }

                // Check if the fragment is attached to an activity before accessing it
                if (isAdded) {
                    this@Order.orderList = orderList
                    orderPlacedAdapter = OrderPlacedAdapter(orderList, databaseReference)
                    recyclerView.layoutManager = GridLayoutManager(requireActivity(), 3)
                    recyclerView.adapter = orderPlacedAdapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun filterOrdersByPreference(preference: String) {
        if (::orderPlacedAdapter.isInitialized && ::orderList.isInitialized) {
            val filteredList = orderList.filter { it.Preference == preference }
            orderPlacedAdapter.updateData(filteredList)
        }
    }
}