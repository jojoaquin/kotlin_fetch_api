package com.joaquin.connectapi.orderCart

data class Product(
    val created_at: String,
    val description: String,
    val id: Int,
    val image: String,
    val name: String,
    val price: Int,
    val stock: Int,
    val updated_at: String
)