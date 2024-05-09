package com.joaquin.connectapi.orderCart

data class OrderDetail(
    val created_at: String,
    val id: Int,
    val id_order: Int,
    val id_product: Int,
    val product: Product,
    val qty: Int,
    val sub_total: Int,
    val updated_at: String
)