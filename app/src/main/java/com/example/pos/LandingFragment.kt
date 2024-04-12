package com.example.pos

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
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
    private var preference: String = "Dine In"
    private lateinit var categoryLayout: LinearLayout
    private var productCache: MutableList<Product>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_landing, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = MyAdapter(requireContext(), mutableListOf(), this)
        orderAdapter = OrderAdapter(requireContext(), mutableListOf(), this)
        recyclerView.adapter = adapter
        databaseRef = FirebaseDatabase.getInstance().reference.child("products")

        categoryLayout = view.findViewById(R.id.categories_layout)

        // Fetch categories from Firebase
        fetchCategoriesFromFirebase()
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

        // Check if productCache is null and fetch data if needed
        if (productCache == null) {
            fetchAllProducts(databaseRef)
        } else {
            // If cache is available, use it to update the adapter
            adapter.updateProductList(productCache!!)
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

    private fun fetchCategoriesFromFirebase() {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("categories")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val categories = mutableListOf<String>()
                dataSnapshot.children.forEach { categorySnapshot ->
                    val categoryName = categorySnapshot.child("Name").getValue(String::class.java)
                    categoryName?.let {
                        categories.add(it)
                    }
                }
                // Once categories are fetched, dynamically add TextViews for each category
                addCategoryTextViews(categories)

                setUpCategoryTextViewListeners()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                Log.e("Firebase", "Error fetching categories: ${databaseError.message}")
            }
        })
    }

    private fun addCategoryTextViews(categories: List<String>) {
        val categoryLayout: LinearLayout = view?.findViewById(R.id.categories_layout) ?: return
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 0, 16, 0) // Adjust margins as needed

        // Add "All" category TextView
        val allTextView = TextView(requireContext())
        allTextView.text = "All"
        allTextView.setPadding(16, 8, 16, 8) // Adjust padding as needed
        allTextView.setBackgroundResource(R.drawable.textview_selector) // Apply background drawable
        allTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // Set text color
        allTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F) // Set text size
        allTextView.layoutParams = layoutParams
        categoryLayout.addView(allTextView)

        // Add other category TextViews
        categories.forEach { categoryName ->
            val categoryTextView = TextView(requireContext())
            categoryTextView.text = categoryName
            categoryTextView.setPadding(16, 8, 16, 8) // Adjust padding as needed
            categoryTextView.setBackgroundResource(R.drawable.textview_selector) // Apply background drawable
            categoryTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // Set text color
            categoryTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F) // Set text size
            categoryTextView.layoutParams = layoutParams
            categoryLayout.addView(categoryTextView)
        }
    }

    private fun setUpCategoryTextViewListeners() {
        // Set up click listeners for dynamically added category TextViews
        categoryLayout.children.forEach { categoryTextView ->
            categoryTextView.setOnClickListener {
                val categoryName = (categoryTextView as TextView).text.toString()
                val filteredProducts = if (categoryName == "All") {
                    allProducts // Show all products if "All" is selected
                } else {
                    allProducts.filter { it.category == categoryName }
                }
                adapter.updateProductList(filteredProducts.toMutableList())
            }
        }
    }


    override fun onAddToCartClicked(product: Product, temperature: String, size: String, price: Int) {
        Log.d("DEBUG", "Add to Cart clicked for product: ${product.productName}, Temperature: $temperature, Size: $size, Price: $price")

        // Find if the item with the same product name, temperature, and size already exists in the order list
        val existingOrderItem = orderList.find { it.productName == product.productName && it.temperature == temperature && it.size == size }

        if (existingOrderItem != null) {
            // If the item already exists, increment its quantity
            existingOrderItem.quantity++
        } else {
            // If the item doesn't exist, add it to the order list
            orderList.add(OrderItem(product.productName, size, price, 1, temperature))
        }

        // Update the order adapter and calculate/display totals
        orderAdapter.updateOrderItemList(orderList)
        calculateAndDisplayTotals(orderList)
    }

    private fun fetchAllProducts(databaseRef: DatabaseReference) {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productList = mutableListOf<Product>()
                val productCount = dataSnapshot.childrenCount.toInt() // Get the number of products
                Log.d("Debug", "Number of products fetched: $productCount")
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

                    val imageURL = productSnapshot.child("imageURL").getValue(String::class.java) ?: ""

                    val product = Product(productName, category, hotVariations, icedVariations, isHot = true, imageURL)
                    productList.add(product)
                }
                productCache = productList
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
            orderItemMap["Variation"] = orderItem.temperature
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