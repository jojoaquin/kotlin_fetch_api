package com.joaquin.connectapi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.joaquin.connectapi.databinding.ActivityMainBinding
import com.joaquin.connectapi.login.Data
import com.joaquin.connectapi.login.User
import com.joaquin.connectapi.login.UserItem
import com.joaquin.connectapi.logincustomer.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAllTable()

        loginProses()

        val btnCustomer = findViewById<Button>(R.id.btnLoginCustomer)

        btnCustomer.setOnClickListener {
            val intent = Intent(this@MainActivity, LoginCustomerActivity::class.java)
            startActivity(intent)

            finish()
        }
    }

    private fun getAllTable() {
//        val response = lifecycleScope.launch {
//            val response = RetrofitInstance.retrofit.getAllTable()
//
//            if (response.isSuccessful && response.body() != null) {
//                Log.d("response", "getALlTable: ${response.body()}" )
//            } else {
//                Log.d("response", "${response.body()}")
//            }
//        }

        lifecycleScope.launch {
            val url = URL("http://192.168.1.6:8000/api/tables") // Gantilah dengan URL API yang sesuai
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            try {
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

                        Log.d("response", "getAllTable: $response")
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
            } finally {
                connection.disconnect()
            }
        }

    }

    private fun loginProses() {
        binding.buttonLogin.setOnClickListener {
            if (binding.etEmail.text.toString().isNotEmpty() && binding.etPassword.text.toString().isNotEmpty()) {
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()

                Log.d("login", "$email $password")
                login(email, password)
            } else {
                Log.d("login", "gagal")
            }
        }
    }

    private fun login(email: String, password: String) {
//        lifecycleScope.launch {
//            val login = UserItem(email, password)
//            val response = RetrofitInstance.retrofit.loginProses(login)
//
//            if (response.code() == 200) {
//
//                val intent = Intent(this@MainActivity, HomeActivity::class.java)
//                startActivity(intent)
//
//                val data = response.body()
//                if (data != null) {
//                    Log.d("hasil login", "name: ${data.data.name}")
//                    Log.d("hasil login", "email: ${data.data.email}")
//                }
//            } else {
//                Toast.makeText(this@MainActivity, "Username atau password salah!", Toast.LENGTH_LONG).show()
//            }
//        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val login = UserItem(email, password)
                val url = URL("http://192.168.1.6:8000/api/login") // Ganti dengan URL API Anda

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 5000
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doOutput = true

                val outputStream = DataOutputStream(connection.outputStream)
                val jsonObject = JSONObject()
                jsonObject.put("email", login.email)
                jsonObject.put("password", login.password)
                val requestBody = jsonObject.toString()

                outputStream.writeBytes(requestBody)
                outputStream.flush()
                outputStream.close()

                val responseCode = connection.responseCode

                if (responseCode == 200) {
                    val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()

                    var line: String?
                    while (inputStream.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    inputStream.close()

                    val responseData = Gson().fromJson(response.toString(), User::class.java)

                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@MainActivity, ListTableActivity::class.java)
                        startActivity(intent)

                        Log.d("hasil login", "name: ${responseData.data.name}")
                        Log.d("hasil login", "email: ${responseData.data.email}")
                    }

                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Email atau password salah!", Toast.LENGTH_LONG).show()
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}