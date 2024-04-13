package com.example.pos

data class ReceiptDetails(
    val customerName: String = "",
    val discount: Int = 0,
    val orderDateTime: String = "",
    val orderItems: Map<String, OrderDetails> = emptyMap(),
    val orderNumber: String = "",
    val preference: String = "",
    val staffName: String = "",
    val subtotal: Double = 0.0,
    val total: Double = 0.0
) {
    constructor() : this("", 0, "", emptyMap(), "", "", "", 0.0, 0.0)
}

data class OrderDetails(
    val price: Double = 0.0,
    val productName: String = "",
    val quantity: Int = 0,
    val size: String = "",
    val variation: String = ""
)