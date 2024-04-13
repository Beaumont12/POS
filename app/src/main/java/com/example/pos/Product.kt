package com.example.pos

data class Product(
    val productName: String,
    val category: String,
    val hotVariations: Map<String, Int>,
    val icedVariations: Map<String, Int>,
    var isHot: Boolean,
    val imageURL: String,
    val stockStatus: String,
)