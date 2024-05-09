package com.joaquin.connectapi

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.joaquin.connectapi.databinding.ActivityDetailProductBinding
import com.joaquin.connectapi.orderCart.OrderRequest
import com.joaquin.connectapi.product.Data
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class DetailProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getIntExtra("productId", 0)

        showProduct(productId)
        plusMinus()
    }

    private fun showProduct(productId: Int) {
        lifecycleScope.launch {
            try {
                val url = URL("http://192.168.1.6:8000/api/product/$productId")
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
                        val dataObject = jsonResponse.optJSONObject("data")

                        if (dataObject != null) {
                            val dataItem = Data(
                                id = dataObject.getInt("id"),
                                name = dataObject.getString("name"),
                                image = dataObject.getString("image"),
                                description = dataObject.getString("description"),
                                price = dataObject.getInt("price"),
                                stock = dataObject.getInt("stock"),
                                created_at = dataObject.getString("created_at"),
                                updated_at = dataObject.getString("updated_at"),
                                category = dataObject.getString("category")
                            )

                            withContext(Dispatchers.Main) {
                                val productName = findViewById<TextView>(R.id.tvDetailName)
                                val productPrice = findViewById<TextView>(R.id.tvDetailPrice)
                                val productDesc = findViewById<TextView>(R.id.tvDetailDesc)
                                val productImage = findViewById<ImageView>(R.id.ivDetailImage)
                                val qty = findViewById<EditText>(R.id.etQty)

                                Picasso.get().load("http://192.168.1.6:8000/image/${dataItem.image}").into(productImage)
                                productName.text = dataItem.name
                                productPrice.text = "Rp. ${dataItem.price}"
                                productDesc.text = dataItem.description
                                qty.setText(1.toString())
                            }

                            binding.btnAddToCart.setOnClickListener {
                                addToCartProses(dataItem.id)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addToCartProses(productId: Int) {
        val tableCode = intent.getStringExtra("tableCode")
        val qty = binding.etQty.text.toString()
        val quantity = qty.toInt()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val body = OrderRequest(quantity, tableCode)
                val url = URL("http://192.168.1.6:8000/api/createorder/$productId")

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 5000
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.doOutput = true

                val outputStream = DataOutputStream(conn.outputStream)
                val requestBody = Gson().toJson(body)
                outputStream.writeBytes(requestBody)
                outputStream.flush()
                outputStream.close()

                val responseCode = conn.responseCode

                if (responseCode == 200) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DetailProductActivity, "Order berhasil dibuat", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("erorr", "Gagal dibuat")
                    Log.d("response", "$productId, $tableCode, $quantity")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun plusMinus() {
        val plus = findViewById<Button>(R.id.btnPlus)

        plus.setOnClickListener {
            val qty = findViewById<EditText>(R.id.etQty)
            val oldQty = qty.text.toString().toIntOrNull() ?: 1
            qty.setText((oldQty + 1).toString())
        }

        val minus = findViewById<Button>(R.id.btnMinus)

        minus.setOnClickListener {
            val qty = findViewById<EditText>(R.id.etQty)
            val oldQty = qty.text.toString().toIntOrNull() ?: 1
            if (oldQty != 1) {
            qty.setText((oldQty - 1).toString())
            } else {
                Toast.makeText(this@DetailProductActivity, "Tidak boleh kurang dari 1", Toast.LENGTH_SHORT).show()
            }
        }


    }


}