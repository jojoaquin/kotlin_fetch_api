package com.joaquin.connectapi.product

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.joaquin.connectapi.DetailProductActivity
import com.joaquin.connectapi.R
import com.squareup.picasso.Picasso

class ProductAdapter(private val context: Context, private val productItem: ProductItem, private val tableCode: String?) : BaseAdapter() {
    override fun getItem(position: Int): Any {
        return productItem.data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return productItem.data.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view: View = View.inflate(context, R.layout.grid_item, null)

        val productName = view.findViewById<TextView>(R.id.tvProductName)
        val productPrice = view.findViewById<TextView>(R.id.tvProductPrice)
        val productImage = view.findViewById<ImageView>(R.id.ivImageProduct)

        val productItems = productItem.data[position]

        productName.text = productItems.name.toString()
        productPrice.text = productItems.price.toString()
        Picasso.get().load("http://192.168.1.6:8000/image/${productItems.image}").into(productImage)

        view.setOnClickListener {
            val intent = Intent(context, DetailProductActivity::class.java)

            intent.putExtra("productId", productItems.id)
            intent.putExtra("tableCode", tableCode)
            context.startActivity(intent)
        }

        return view
    }

}