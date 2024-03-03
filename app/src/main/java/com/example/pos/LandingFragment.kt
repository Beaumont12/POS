package com.example.pos

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class LandingFragment(private val loggedInUserName: String) : Fragment(), MyAdapter.OnAddToCartClickListener, OnOrderChangedListener {

    private lateinit var orderAdapter: OrderAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private lateinit var allProducts: MutableList<Product>
    private lateinit var orderList: MutableList<OrderItem>
    private lateinit var databaseRef: DatabaseReference
    private var customerName: String = ""
    private var selectedDiscount: String = "None"
    private var lastClickedTemperature: String = "hot"
    private var preference: String = "Dine In" // Default preference is Dine In

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_landing, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = MyAdapter(requireContext(), mutableListOf(), this)
        orderAdapter = OrderAdapter(requireContext(), mutableListOf(), this)
        recyclerView.adapter = adapter
        databaseRef = FirebaseDatabase.getInstance().reference.child("products") // Initialize databaseRef
        return view
    }

    override fun onOrderChanged(orderItemList: List<OrderItem>) {
        orderAdapter.updateOrderItemList(orderItemList.toMutableList())
        calculateAndDisplayTotals(orderItemList.toMutableList())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Initialize allProducts list
        allProducts = mutableListOf()
        orderList = mutableListOf()

        // Retrieve all products initially
        fetchAllProducts(databaseRef)

        val orderNumber = generateOrderNumber() // Call the function
        val orderNumberTextView = view.findViewById<TextView>(R.id.order_number)
        orderNumberTextView?.text = "Order #$orderNumber"

        val buttonToggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.choose_button_group)
        buttonToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val selectedButton = view.findViewById<MaterialButton>(checkedId)
                preference = when (selectedButton.id) {
                    R.id.dinein_button -> "Dine In"
                    R.id.takeout_button -> "Take Out"
                    else -> "Dine In" // Default to Dine In if none selected
                }
            }
        }

        val customerNameEditText = view.findViewById<EditText>(R.id.customer_name)
        customerNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Update customerName when text changes
                customerName = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                // No implementation needed
            }
        })
        // Initialize search bar
        val searchBar = view.findViewById<EditText>(R.id.search_bar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter the products based on the entered text
                val searchText = s.toString().trim().toLowerCase(Locale.getDefault())
                val filteredProducts = if (searchText.isEmpty()) {
                    allProducts // Show all products if search text is empty
                } else {
                    allProducts.filter { it.productName.toLowerCase(Locale.getDefault()).contains(searchText) }
                }
                adapter.updateProductList(filteredProducts.toMutableList())
            }

            override fun afterTextChanged(s: Editable?) {
                // No implementation needed
            }
        })

        // Click listener for "All" category
        view.findViewById<View>(R.id.all_category).setOnClickListener {
            adapter.updateProductList(allProducts)
        }

        // Click listener for "Specials" category
        view.findViewById<View>(R.id.specials_category).setOnClickListener {
            val specialsProducts = allProducts.filter { it.category == "specials" }
            adapter.updateProductList(specialsProducts.toMutableList())
        }

        // Click listener for "Coffee" category (example)
        view.findViewById<View>(R.id.coffee_category).setOnClickListener {
            val coffeeProducts = allProducts.filter { it.category == "coffee" }
            adapter.updateProductList(coffeeProducts.toMutableList())
        }

        view.findViewById<View>(R.id.tea_category).setOnClickListener {
            val teaProducts = allProducts.filter { it.category == "tea" }
            adapter.updateProductList(teaProducts.toMutableList())
        }

        view.findViewById<View>(R.id.lemonade_category).setOnClickListener {
            val lemonadeProducts = allProducts.filter { it.category == "lemonade" }
            adapter.updateProductList(lemonadeProducts.toMutableList())
        }

        view.findViewById<View>(R.id.frappe_category).setOnClickListener {
            val frappeProducts = allProducts.filter { it.category == "frappe" }
            adapter.updateProductList(frappeProducts.toMutableList())
        }

        view.findViewById<View>(R.id.tumblers_category).setOnClickListener {
            val tumblersProducts = allProducts.filter { it.category == "tumblers" }
            adapter.updateProductList(tumblersProducts.toMutableList())
        }

        view.findViewById<View>(R.id.pastries_category).setOnClickListener {
            val pastriesProducts = allProducts.filter { it.category == "pastries" }
            adapter.updateProductList(pastriesProducts.toMutableList())
        }

        // Add click listener to clear all orders
        view.findViewById<TextView>(R.id.clear_all).setOnClickListener {
            orderList.clear()
            orderAdapter.updateOrderItemList(orderList)
            calculateAndDisplayTotals(orderList)
        }

        view.findViewById<Button>(R.id.button_hot)?.setOnClickListener {
            lastClickedTemperature = "hot"
            // Handle hot button click
        }

        // Click listener for the iced button
        view.findViewById<Button>(R.id.button_iced)?.setOnClickListener {
            lastClickedTemperature = "iced"
            // Handle iced button click
        }

        // Set up adapter for order list
        val orderRecyclerView: RecyclerView = view.findViewById(R.id.order_list_preview)
        orderRecyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        orderRecyclerView.adapter = orderAdapter

        // Setup spinner for discounts
        val discountSpinner: Spinner = view.findViewById(R.id.discount_spinner)

        // Create an array list containing the discount options
        val discounts = resources.getStringArray(R.array.discount_options).toMutableList()

        // Add the default value to the list
        val defaultValue = "Select Discount"
        discounts.add(0, defaultValue)

        // Create the adapter
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, discounts)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        discountSpinner.adapter = adapter

        // Set the default value as the initial selection
        val defaultIndex = 0
        discountSpinner.setSelection(defaultIndex)

        discountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Handle discount selection here
                selectedDiscount = parent?.getItemAtPosition(position).toString()
                calculateAndDisplayTotals(orderList)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where nothing is selected
            }
        }

        // Click listener for confirm button
        view.findViewById<Button>(R.id.confirm_button).setOnClickListener {
            if (customerName.isBlank()) {
                Toast.makeText(requireContext(), "Please enter customer name", Toast.LENGTH_SHORT).show()
            }else if(orderList.isEmpty()) {
                Toast.makeText(requireContext(), "No orders to print", Toast.LENGTH_SHORT).show()
            }
            else {
                confirmOrderWithDialog(orderNumber)
            }
        }
    }

    override fun onAddToCartClicked(product: Product) {
        // Get the displayed price
        val displayedPrice = getDisplayedPriceFromRecyclerView(product)

        // Get the size based on the displayed price
        val size = getSizeBasedOnDisplayedPrice(product, displayedPrice)

        // Find if the item with the same product name and size already exists in the order list
        val existingOrderItem = orderList.find { it.productName == product.productName && it.size == size }

        if (existingOrderItem != null) {
            // If the item already exists, increment its quantity
            existingOrderItem.quantity++
        } else {
            // If the item doesn't exist, add it to the order list
            orderList.add(OrderItem(product.productName, size, displayedPrice, 1))
        }

        // Update the order adapter and calculate/display totals
        orderAdapter.updateOrderItemList(orderList)
        calculateAndDisplayTotals(orderList)
    }

    private fun getSizeBasedOnDisplayedPrice(product: Product, displayedPrice: Int): String {
        val variations = if (product.isHot) product.hotVariations else product.icedVariations

        for ((size, price) in variations) {
            if (price == displayedPrice) {
                return size
            }
        }

        // If no match is found, you can handle this case based on your requirements
        // For example, return an empty string or a default size
        return ""
    }

    private fun getDisplayedPriceFromRecyclerView(product: Product): Int {
        val recyclerView = recyclerView  // Reference to your RecyclerView instance

        // Iterate through the RecyclerView items to find the one corresponding to the given product
        for (i in 0 until recyclerView.childCount) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? MyAdapter.ViewHolder
            viewHolder?.let {
                val productName = it.textProductName.text.toString()

                // Check if the product name matches
                if (productName.contains(product.productName)) {
                    val sizeAndPrice = productName.split("  ").lastOrNull() ?: ""
                    val price = sizeAndPrice.split(" - ").lastOrNull()?.substring(1)?.toIntOrNull() ?: 0

                    if (price > 0) {
                        // If the price is valid, return it
                        return price
                    }
                }
            }
        }

        // If the displayed price is not found, return a default value
        return 0
    }


    private fun fetchAllProducts(databaseRef: DatabaseReference) {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productList = mutableListOf<Product>()
                for (productSnapshot in dataSnapshot.children) {
                    val productName = productSnapshot.child("Name").getValue(String::class.java) ?: ""
                    val category = productSnapshot.child("Category").getValue(String::class.java) ?: ""
                    val hotVariationsSnapshot = productSnapshot.child("Variations").child("temperature").child("hot")
                    val icedVariationsSnapshot = productSnapshot.child("Variations").child("temperature").child("iced")
                    val hotVariations = mutableMapOf<String, Int>()
                    val icedVariations = mutableMapOf<String, Int>()

                    for (sizeSnapshot in hotVariationsSnapshot.children) {
                        val size = sizeSnapshot.key ?: ""
                        val price = sizeSnapshot.getValue(Int::class.java) ?: 0
                        hotVariations[size] = price
                    }

                    for (sizeSnapshot in icedVariationsSnapshot.children) {
                        val size = sizeSnapshot.key ?: ""
                        val price = sizeSnapshot.getValue(Int::class.java) ?: 0
                        icedVariations[size] = price
                    }

                    val product = Product(productName, category, hotVariations, icedVariations, isHot = true)
                    productList.add(product)
                }
                allProducts = productList
                adapter.updateProductList(allProducts)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }

    private fun calculateAndDisplayTotals(orderList: MutableList<OrderItem>) {
        var subtotal = 0
        for (orderItem in orderList) {
            subtotal += orderItem.price * orderItem.quantity // Calculate subtotal dynamically
        }

        val discountPercentage = when (selectedDiscount) {
            "Senior Citizen", "PWD" -> 20
            "Student" -> 8
            else -> 0
        }
        val discount = (subtotal * (discountPercentage / 100.0)).toInt()
        val total = subtotal - discount

        val subtotalTextView = view?.findViewById<TextView>(R.id.subtotal)
        val discountTextView = view?.findViewById<TextView>(R.id.discount)
        val totalTextView = view?.findViewById<TextView>(R.id.total)

        // Update TextViews with calculated values
        subtotalTextView?.text = "P$subtotal"
        discountTextView?.text = "P$discount"
        totalTextView?.text = "P$total"
    }

    private fun generateOrderNumber(): Int {
        // Get current timestamp
        val timestamp = System.currentTimeMillis()

        // Extract the last 4 digits of the timestamp
        val lastFourDigits = (timestamp % 10000).toInt()

        // Generate a random number between 1000 and 9999 (4 digits)
        val random = Random.nextInt(9000) + 1000

        // Combine the last four digits of the timestamp with the random number
        val orderNumber = "$lastFourDigits$random".toInt()

        return orderNumber
    }

    private fun confirmOrderWithDialog(orderNumber: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Order")
        builder.setMessage("Are you sure you want to confirm this order?")
        builder.setPositiveButton("Yes") { dialog, which ->
            // User clicked Yes button, proceed with order confirmation
            val orderNumber = generateOrderNumber() // Generate a new order number
            confirmOrder(orderNumber) // Pass the order number to confirmOrder
        }
        builder.setNegativeButton("No") { dialog, which ->
            // User clicked No button, dismiss the dialog
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    private fun confirmOrder(orderNumber: Int) {
        val ordersRef = FirebaseDatabase.getInstance().reference.child("orders").child(orderNumber.toString())
        val orderMap = mutableMapOf<String, Any>()
        val currentDateTime = getCurrentDateTime()
        val currentDateTimeString = currentDateTime.toString()

        // Calculate subtotal, discount, and total
        val subtotal = orderList.sumBy { it.price * it.quantity }
        val discountPercentage = when (selectedDiscount) {
            "Senior Citizen", "PWD" -> 20
            "Student" -> 8
            else -> 0
        }
        val discount = (subtotal * (discountPercentage / 100.0)).toInt()
        val total = subtotal - discount

        // Add staff name to the order data
        orderMap["StaffName"] = loggedInUserName
        orderMap["OrderDateTime"] = currentDateTimeString
        orderMap["CustomerName"] = customerName
        orderMap["Preference"] = preference
        orderMap["Subtotal"] = subtotal
        orderMap["Discount"] = discount
        orderMap["Total"] = total


        // Add order items to the map
        orderList.forEachIndexed { index, orderItem ->
            val orderItemMap = mutableMapOf<String, Any>()
            orderItemMap["ProductName"] = orderItem.productName
            orderItemMap["Size"] = orderItem.size
            orderItemMap["Price"] = orderItem.price
            orderItemMap["Quantity"] = orderItem.quantity
            orderMap["Order_${index + 1}"] = orderItemMap
        }

        ordersRef.updateChildren(orderMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Clear the order list and customer name
                orderList.clear()
                orderAdapter.updateOrderItemList(orderList)
                calculateAndDisplayTotals(orderList)
                clearCustomerName()

                // Update the order number TextView with the new order number
                val orderNumberTextView = view?.findViewById<TextView>(R.id.order_number)
                orderNumberTextView?.text = "Order #$orderNumber"
            } else {
                // Handle error
            }
        }
    }

    private fun clearCustomerName() {
        val customerNameEditText = view?.findViewById<EditText>(R.id.customer_name)
        customerNameEditText?.setText("") // Clear the text in the customer name EditText
        customerName = ""
    }
}