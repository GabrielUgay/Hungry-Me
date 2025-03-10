package com.example.hungryme

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MainActivity4 : AppCompatActivity() {
    private lateinit var backToLoginPage: ImageView
    private lateinit var resetPassword: FrameLayout
    private lateinit var emailInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main4)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        emailInput = findViewById(R.id.emailInput)
        backToLoginPage = findViewById(R.id.backToLoginPage)
        resetPassword = findViewById(R.id.resetPassword)

        backToLoginPage.setOnClickListener {
            navigateToLogin()
        }

        resetPassword.setOnClickListener {
            val yourEmail = emailInput.text.toString().trim()
            if (isValidEmail(yourEmail)) {
                checkEmailExists(yourEmail)
            } else {
                showToast("Please enter a valid email address")
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun checkEmailExists(email: String) {
        val queue: RequestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Request.Method.POST, Constants.URL_CHECK_EMAIL, // Using Constants class
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.getBoolean("success")
                    val message = jsonResponse.getString("message")

                    if (success) {
                        navigateToResetPassword(email)
                    } else {
                        showToast(message)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast("Error parsing response")
                }
            },
            Response.ErrorListener {
                showToast("Network error, please try again")
            }) {

            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("email" to email)
            }
        }

        queue.add(stringRequest)
    }

    private fun navigateToResetPassword(email: String) {
        val intent = Intent(this, MainActivity14::class.java).apply {
            putExtra("email", email)
        }
        startActivity(intent)
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, MainActivity2::class.java))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
