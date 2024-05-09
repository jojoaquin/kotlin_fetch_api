package com.joaquin.connectapi.orderCart

data class OrderHeader(
    val created_at: String,
    val date: String,
    val id: Int,
    val id_table: Int,
    val status: String,
    val sub_total: Int,
    val updated_at: String
)