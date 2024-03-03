package com.example.pos

data class OrderData(
    var orderNumber: String? = "",
    val CustomerName: String? = "",
    val Discount: Int? = 0,
    val OrderDateTime: String? = "",
    val Preference: String? = "",
    val StaffName: String? = "",
    val Subtotal: Double? = 0.0,
    val Total: Double? = 0.0,
    var orderItems: Map<String, OrderList>? = null // Map to store dynamic order items // List of ordered items
)