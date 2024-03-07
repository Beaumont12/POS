package com.example.pos

data class HistoryData(
    val orderNumber: String,
    val staffName: String,
    val orderDateTime: String,
    val total: Int,
    val orderItems: Map<String, Int> // Map of order items
)
