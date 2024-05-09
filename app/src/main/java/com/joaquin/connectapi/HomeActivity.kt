package com.joaquin.connectapi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.GridView
import androidx.lifecycle.lifecycleScope
import com.joaquin.connectapi.databinding.ActivityHomeBinding
import com.joaquin.connectapi.product.Data
import com.joaquin.connectapi.product.ProductAdapter
import com.joaquin.connectapi.product.ProductItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAllProduct()
        bottomBar()
    }

    private fun bottomBar() {

        binding.tvHome.setOnClickListener {
            false
        }

        val idTable = intent.getIntExtra("idTable", 0)
        binding.tvCart.setOnClickListener {
            val intent = Intent(this@HomeActivity, CartActivity::class.java)
            intent.putExtra("idTable", idTable)
            startActivity(intent)
        }
    }

    private fun setUpGridView(productItem: ProductItem) {
        val tableCode = intent.getStringExtra("tableCode")

        val productGridView = findViewById<GridView>(R.id.ProductGridView)
        val adapter = ProductAdapter(this, productItem, tableCode)
        productGridView.adapter = adapter
    }


    //yFti0Z

    private fun getAllProduct() {
        lifecycleScope.launch {
            try {
                val url = URL("http://192.168.1.6:8000/api/products")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                withContext(Dispatchers.IO) {
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        val response = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        reader.close()

                        val jsonResponse = JSONObject(response.toString())
                        val dataArray = jsonResponse.optJSONArray("data")

                        if (dataArray != null) {
                            val dataList = mutableListOf<Data>()

                            for (i in 0 until  dataArray.length()) {
                                val dataObject = dataArray.getJSONObject(i)
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
                                dataList.add(dataItem)
                            }
                            val productItem = ProductItem(dataList)
                            withContext(Dispatchers.Main) {
                                setUpGridView(productItem)
                            }
                        } else {
                            Log.d("response", "Data is null")
                        }

                        Log.d("response", "Response: $response")
                    } else {
                        val errorStream = connection.errorStream
                        val reader = BufferedReader(InputStreamReader(errorStream))
                        val errorResponse = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            errorResponse.append(line)
                        }
                        reader.close()

                        Log.d("response", "Error: $errorResponse")
                    }
                }
            } catch (e: Exception) {
                Log.e("response", "Error: ${e.message}")
            }
        }

   }

}