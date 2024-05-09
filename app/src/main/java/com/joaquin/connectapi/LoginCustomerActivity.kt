package com.joaquin.connectapi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.joaquin.connectapi.databinding.ActivityLoginCustomerBinding
import com.joaquin.connectapi.logincustomer.Customer
import com.joaquin.connectapi.logincustomer.CustomerRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class LoginCustomerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginCustomerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginProses()

        val btnLoginStaff = findViewById<Button>(R.id.btnLoginStaff)
        btnLoginStaff.setOnClickListener {
            val intent = Intent(this@LoginCustomerActivity, MainActivity::class.java)
            startActivity(intent)

            finish()
        }
    }

    private fun loginProses() {
        binding.buttonLogin.setOnClickListener {
            if (binding.etCode.text.toString().isNotEmpty()) {
                val code = binding.etCode.text.toString()
                makeLogin(code)
            } else {
                Toast.makeText(this@LoginCustomerActivity, "Code tidak boleh kosong", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun makeLogin(code: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val login = CustomerRequest(code)
                val url = URL("http://192.168.1.6:8000/api/table")

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 5000
                conn.setRequestProperty("Content-type", "application/json; charset=UTF-8")
                conn.doOutput = true

                val outputStream = DataOutputStream(conn.outputStream)
                val requestBody = Gson().toJson(login)
                outputStream.writeBytes(requestBody)
                outputStream.flush()
                outputStream.close()

                val responseCode = conn.responseCode

                if (responseCode == 200) {
                    val inputStream = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = StringBuilder()

                    var line: String?
                    while (inputStream.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    inputStream.close()

                    val responseData = Gson().fromJson(response.toString(), Customer::class.java)

                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@LoginCustomerActivity, HomeActivity::class.java)
                        intent.putExtra("tableCode", responseData.data.code)
                        intent.putExtra("idTable", responseData.data.name)
                        startActivity(intent)

                        Log.d("response", "${responseData.data.code}")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginCustomerActivity, "Username atau password salah!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}