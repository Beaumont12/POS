package com.example.pos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class History : Fragment() {
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerView = view.findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = HistoryAdapter(emptyList())
        recyclerView.adapter = historyAdapter
        val printButton = view.findViewById<Button>(R.id.print_button)

        printButton.setOnClickListener {
            // Start PrintActivity when the button is clicked
            startActivity(Intent(requireContext(), PrintActivity::class.java))
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("history")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val historyList = mutableListOf<HistoryData>()
                for (dataSnapshot in snapshot.children) {
                    val orderNumber = dataSnapshot.child("orderNumber").value.toString()
                    val staffName = dataSnapshot.child("staffName").value.toString()
                    val orderDateTime = dataSnapshot.child("orderDateTime").value.toString()
                    val total = dataSnapshot.child("total").value.toString().toInt()

                    val orderItems = mutableMapOf<String, Int>()
                    for (itemSnapshot in dataSnapshot.child("orderItems").children) {
                        val productName = itemSnapshot.child("productName").value.toString()
                        val quantity = itemSnapshot.child("quantity").value.toString().toInt()
                        orderItems[productName] = quantity
                    }

                    val historyData = HistoryData(orderNumber, staffName, orderDateTime, total, orderItems)
                    historyList.add(historyData)
                }
                historyAdapter.updateData(historyList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        return view
    }
}