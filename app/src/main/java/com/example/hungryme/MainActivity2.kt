package com.example.hungryme

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject

class MainActivity2 : AppCompatActivity() {
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var signUp: ImageView
    private lateinit var forgotPassword: TextView
    private lateinit var loginBtn: ImageView
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Views
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)

        signUp = findViewById(R.id.register)
        forgotPassword = findViewById(R.id.forgotPassword)
        loginBtn = findViewById(R.id.login)

        // Initialize Progress Dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")

        // Set Click Listeners
        signUp.setOnClickListener {
            startActivity(Intent(this, MainActivity3::class.java))
        }

        forgotPassword.setOnClickListener {
            startActivity(Intent(this, MainActivity4::class.java))
        }

        loginBtn.setOnClickListener {
            userLogin()
        }
    }

    private fun userLogin() {
        val username = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.show()

        val stringRequest = object : StringRequest(
            Request.Method.POST,
            Constants.URL_LOGIN,
            Response.Listener { response ->
                progressDialog.dismiss()
                try {
                    // Debugging - Print raw response
                    println("DEBUG: Raw Response -> $response")

                    val obj = JSONObject(response)

                    if (!obj.getBoolean("error")) {
                        println("DEBUG: Login successful, user data -> ${obj.toString()}")

                        SharedPrefManager.getInstance(applicationContext).userLogin(
                            obj.getInt("id"),
                            obj.getString("username"),
                            obj.getString("email")
                        )

                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        val user = editTextUsername.text.toString().trim()
                        val intent = Intent(this, MainActivity7::class.java)
                        intent.putExtra("user", user)
                        startActivity(intent)
                        finish()
                    } else {
                        println("DEBUG: Login failed, message -> ${obj.getString("message")}")
                        Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                progressDialog.dismiss()
                println("DEBUG: Network error -> ${error.message}")
                Toast.makeText(this, "Login failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                return mapOf("username" to username, "password" to password)
            }
        }

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest)
    }

}
