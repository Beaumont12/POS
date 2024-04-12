package com.example.pos

data class OrderItem(
    val productName: String,
    val size: String, // Size is now the key
    val price: Int, // Price corresponding to the size
    var quantity: Int,
    var temperature: String,
    var deleted: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderItem) return false

        if (productName != other.productName) return false
        if (size != other.size) return false
        if (price != other.price) return false

        return true
    }

    override fun hashCode(): Int {
        var result = productName.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + price
        return result
    }
}