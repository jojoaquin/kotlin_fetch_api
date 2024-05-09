package com.joaquin.connectapi

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.gson.Gson
import com.joaquin.connectapi.databinding.ActivityHomeBinding
import com.joaquin.connectapi.databinding.ActivityListTableBinding
import com.joaquin.connectapi.databinding.InputTableBinding
import com.joaquin.connectapi.table.Data
import com.joaquin.connectapi.table.ListTableAdapter
import com.joaquin.connectapi.table.TableItem
import com.joaquin.connectapi.table.TableRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ListTableActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListTableBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListTableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getListTable()

        binding.floatingActionButton.setOnClickListener {
            addPostTable()
        }

    }

    private fun addPostTable() {
        val mDialog = Dialog(this)
        val mBinding = InputTableBinding.inflate(layoutInflater)

        mDialog.setContentView(mBinding.root)

        mBinding.btnTableClose.setOnClickListener {
            mDialog.dismiss()
        }

        mBinding.btnTableSubmit.setOnClickListener {
            val tableName = mBinding.etTableName.text.toString()
            val tableNumber = tableName.toInt()
            if (tableNumber < 1) {
                Toast.makeText(this@ListTableActivity, "Tidak boleh kurang dari 1", Toast.LENGTH_SHORT).show()
            } else if (tableNumber > 50) {
                Toast.makeText(this@ListTableActivity, "Tidak boleh lebih dari 50", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val table = TableRequest(tableNumber)
                        val url = URL("http://192.168.1.6:8000/api/createtable")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"
                        conn.doOutput = true
                        conn.setRequestProperty("Content-type", "application/json; charset=UTF-8")
                        conn.connectTimeout = 5000

                        val outputStream = DataOutputStream(conn.outputStream)
                        val requestBody = Gson().toJson(table)
                        outputStream.writeBytes(requestBody)
                        outputStream.flush()
                        outputStream.close()

                        if (conn.responseCode == 200) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@ListTableActivity, "Table berhasil dibuat", Toast.LENGTH_SHORT).show()
                                getListTable()

                                mDialog.dismiss()
                            }
                        } else {
                            Log.d("response", "gagal")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        mDialog.show()
    }

    private fun setupRecyclerView(tableItem: TableItem) {
        val recyclerView = findViewById<RecyclerView>(R.id.rvListTable)
        val layoutManager = LinearLayoutManager(this@ListTableActivity)
        recyclerView.layoutManager = layoutManager
        val adapter = ListTableAdapter(tableItem)
        recyclerView.adapter = adapter

    }

    private fun getListTable() {
        lifecycleScope.launch {
            try {
                val url = URL("http://192.168.1.6:8000/api/tables")

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                withContext(Dispatchers.IO) {
                    if (conn.responseCode == 200) {
                        val reader = BufferedReader(InputStreamReader(conn.inputStream))
                        val response = StringBuilder()

                        var line: String?

                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        reader.close()

                        val jsonResponse = JSONObject(response.toString())
                        val dataTableArray = jsonResponse.optJSONArray("data")

                        if (dataTableArray != null) {
                            val dataList = mutableListOf<Data>()

                            for (i in 0 until dataTableArray.length()) {
                                val dataObject = dataTableArray.getJSONObject(i)

                                val dataItem = Data(
                                    code = dataObject.getString("code"),
                                    name = dataObject.getInt("name"),
                                    id = dataObject.getInt("id"),
                                    created_at = dataObject.getString("created_at"),
                                    updated_at = dataObject.getString("updated_at"),
                                    sub_total = dataObject.getInt("sub_total")
                                )

                                dataList.add(dataItem)
                                Log.d("response", dataItem.toString())
                            }

                            val tableItem = TableItem(dataList)
                            withContext(Dispatchers.Main) {
                                setupRecyclerView(tableItem)
                            }

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