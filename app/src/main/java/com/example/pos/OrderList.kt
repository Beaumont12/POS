package com.example.pos

data class OrderList(
    val Price: Double? = 0.0,
    val ProductName: String? = "",
    val Quantity: Int? = 0,
    val Size: String? = ""
)
