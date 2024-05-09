package com.joaquin.connectapi.orderCart

import android.content.Context
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.joaquin.connectapi.R
import com.joaquin.connectapi.product.ProductAdapter
import com.squareup.picasso.Picasso
import org.w3c.dom.Text
import java.net.HttpURLConnection
import java.net.URL

class CartAdapter(
    private val orderItem: OrderItem,
    private val subTotal: Int,
    private val onDeleteClick: (orderItemId: Int) -> Unit,
    private val onPlusUpdate: (orderItemId: Int, qty: Int) -> Unit,
    private val onMinusUpdate: (orderItemId: Int, qty: Int) -> Unit) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.ivImage)
        val productName: TextView = itemView.findViewById(R.id.tvName)
        val productPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val qty: EditText = itemView.findViewById(R.id.etQtyCart)
        val btnDelete: Button = itemView.findViewById(R.id.btnDeleteCart)
        val btnPlus: Button = itemView.findViewById(R.id.btnPlusCart)
        val btnMinus: Button = itemView.findViewById(R.id.btnMinusCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return orderItem.orderDetail.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orderItems = orderItem.orderDetail[position]

        Picasso.get().load("http://192.168.1.6:8000/image/${orderItems.product.image}")
            .into(holder.productImage)
        holder.productName.text = orderItems.product.name.toString()
        holder.productPrice.text = "Rp. ${orderItems.product.price.toString()}"
        holder.qty.setText(orderItems.qty.toString())

        holder.btnDelete.setOnClickListener {
            onDeleteClick(orderItems.id)
        }

        holder.btnPlus.setOnClickListener {
            val qtyText = holder.qty.text.toString()
            val olderQty = qtyText.toInt()
            val newQty = olderQty + 1
            onPlusUpdate(orderItems.id, newQty)
        }

        holder.btnMinus.setOnClickListener {
            val qtyText = holder.qty.text.toString()
            val olderQty = qtyText.toInt()
            val newQty = olderQty - 1
            onMinusUpdate(orderItems.id, newQty)
        }
    }
}