package com.joaquin.connectapi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.joaquin.connectapi.databinding.ActivityCartBinding
import com.joaquin.connectapi.orderCart.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class CartActivity: AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idTable = intent.getIntExtra("idTable", 0)

        getCart(idTable)
        bottomBar()

    }

    private fun bottomBar() {

        binding.tvHome.setOnClickListener {
            finish()
        }

        binding.tvCart.setOnClickListener {
            false
        }
    }

    private fun setRecyclerView(orderItem: OrderItem, subTotal: Int) {
        val recyclerView = findViewById<RecyclerView>(R.id.rvCart)
        val layoutManager = LinearLayoutManager(this@CartActivity) // Choose the appropriate layout manager
        recyclerView.layoutManager = layoutManager
        val adapter = CartAdapter(orderItem, subTotal, onDeleteClick = {
                orderItemId ->
            lifecycleScope.launch(Dispatchers.IO) {
                val url = URL("http://192.168.1.6:8000/api/deleteorder/${orderItemId}")

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "DELETE"

                val responseCode = conn.responseCode

                if (responseCode == 200) {
                    withContext(Dispatchers.Main) {
                        Log.d("response", "good delete")
                        Toast.makeText(this@CartActivity, "Cart berhasil didelete", Toast.LENGTH_SHORT).show()
                        val idTable = intent.getIntExtra("idTable", 0)
                        getCart(idTable)
                    }
                } else {
                    Log.d("response", "gagal")
                }
            }
        }, onPlusUpdate = {
            orderItemId, qty ->
            lifecycleScope.launch(Dispatchers.IO) {
                val updateOrder = OrderUpdate(qty)
                val url = URL("http://192.168.1.6:8000/api/editorder/${orderItemId}")

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "PUT"
                conn.setRequestProperty("Content-type", "application/json; charset=UTF-8")
                conn.connectTimeout = 5000
                conn.doOutput = true

                val outputStream = DataOutputStream(conn.outputStream)
                val requestBody = Gson().toJson(updateOrder)
                outputStream.writeBytes(requestBody)
                outputStream.flush()
                outputStream.close()



                val responseCode = conn.responseCode

                if (responseCode == 200) {
                    withContext(Dispatchers.Main) {
                        Log.d("response", "good edit")
                        Toast.makeText(this@CartActivity, "Cart berhasil diedit", Toast.LENGTH_SHORT).show()
                        val idTable = intent.getIntExtra("idTable", 0)
                        getCart(idTable)
                    }
                } else {
                    Log.d("response", "gagal")
                }

            }
        }, onMinusUpdate = {
            orderItemId, qty ->
            if (qty > 0) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val updateOrder = OrderUpdate(qty)
                    val url = URL("http://192.168.1.6:8000/api/editorder/${orderItemId}")

                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "PUT"
                    conn.setRequestProperty("Content-type", "application/json; charset=UTF-8")
                    conn.connectTimeout = 5000
                    conn.doOutput = true

                    val outputStream = DataOutputStream(conn.outputStream)
                    val requestBody = Gson().toJson(updateOrder)
                    outputStream.writeBytes(requestBody)
                    outputStream.flush()
                    outputStream.close()

                    val responseCode = conn.responseCode

                    if (responseCode == 200) {
                        withContext(Dispatchers.Main) {
                            Log.d("response", "good edit")
                            Toast.makeText(this@CartActivity, "Cart berhasil diedit", Toast.LENGTH_SHORT).show()
                            val idTable = intent.getIntExtra("idTable", 0)
                            getCart(idTable)
                        }
                    } else {
                        Log.d("response", "gagal")
                    }
                }
            } else {
                Toast.makeText(this@CartActivity, "Tidak boleh kurang dari 1", Toast.LENGTH_SHORT).show()
            }
        })
        recyclerView.adapter = adapter
    }

    private fun getCart(idTable: Int) {
        lifecycleScope.launch {
            try {
                val url = URL("http://192.168.1.6:8000/api/cart/$idTable")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                withContext(Dispatchers.IO) {
                    val responseCode = conn.responseCode

                    if (responseCode == 200) {
                        val reader = BufferedReader(InputStreamReader(conn.inputStream))
                        val response = StringBuilder()

                        var line: String?

                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        reader.close()

                        val jsonResponse = JSONObject(response.toString())
                        val dataArrayOrderDetail = jsonResponse.optJSONArray("orderDetail")
                        val dataOrderHeader = jsonResponse.optJSONObject("orderHeader")

                        if (dataArrayOrderDetail != null) {
                            val dataList = mutableListOf<OrderDetail>()

                            for (i in 0 until dataArrayOrderDetail.length()) {
                                val dataOrderDetail = dataArrayOrderDetail.getJSONObject(i)
                                val productObject = dataOrderDetail.getJSONObject("product")

                                val dataItem = OrderDetail(
                                    id = dataOrderDetail.getInt("id"),
                                    id_order = dataOrderDetail.getInt("id_order"),
                                    id_product = dataOrderDetail.getInt("id_product"),
                                    qty = dataOrderDetail.getInt("qty"),
                                    sub_total = dataOrderDetail.getInt("sub_total"),
                                    updated_at = dataOrderDetail.getString("updated_at"), 
                                    created_at = dataOrderDetail.getString("updated_at"),
                                    product = Product(
                                        id = productObject.getInt("id"),
                                        name = productObject.getString("name"),
                                        image = productObject.getString("image"),
                                        description = productObject.getString("description"),
                                        price = productObject.getInt("price"),
                                        stock = productObject.getInt("stock"),
                                        updated_at = productObject.getString("updated_at"),
                                        created_at = productObject.getString("created_at"),
                                    )
                                )
                                dataList.add(dataItem)
                                Log.d("DataItem", dataItem.toString())
                                Log.d("Product", dataItem.product.toString())
                            }
                            val orderItem = OrderItem(dataList)

                            val dataOrderHeaderItem = OrderHeader(
                                sub_total = dataOrderHeader.getInt("sub_total"),
                                id = dataOrderHeader.getInt("id"),
                                id_table = dataOrderHeader.getInt("id_table"),
                                status = dataOrderHeader.getString("status"),
                                created_at = dataOrderHeader.getString("created_at"),
                                updated_at = dataOrderHeader.getString("updated_at"),
                                date = dataOrderHeader.getString("date")
                            )
                            withContext(Dispatchers.Main) {
                                binding.tvTotal.text = "Rp. ${dataOrderHeaderItem.sub_total}"
                                setRecyclerView(orderItem, dataOrderHeaderItem.sub_total)
                            }

                            Log.d("DataHeader", dataOrderHeaderItem.sub_total.toString())
                        } else {
                            Log.d("response", "Data is null")
                        }
                    } else {
                        Log.d("response", "gagal")
                    }
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}