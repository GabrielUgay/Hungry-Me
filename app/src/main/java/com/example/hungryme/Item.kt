package com.example.hungryme

data class Item(
    val id: Int,
    val name: String,
    val price: Double,
    val stock: Int,
    val category: String,
    val restaurant: String,
    val file: String
)