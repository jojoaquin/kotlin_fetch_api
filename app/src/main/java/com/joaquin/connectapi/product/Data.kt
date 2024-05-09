package com.joaquin.connectapi.product

data class Data(
    val created_at: String,
    val description: String,
    val id: Int,
    val image: String,
    val name: String,
    val price: Int,
    val stock: Int,
    val category: String,
    val updated_at: String
)