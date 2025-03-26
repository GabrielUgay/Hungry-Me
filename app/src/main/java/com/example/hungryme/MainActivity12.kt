package com.example.hungryme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity12 : AppCompatActivity() {

    lateinit var date: TextView

    lateinit var price: TextView
    lateinit var totalPayment: TextView
    lateinit var tax: TextView
    lateinit var total: TextView

    lateinit var homePage: ImageView
    lateinit var username: TextView

    lateinit var emailText: TextView
    lateinit var phoneText: TextView

    private val requestQueue by lazy { Volley.newRequestQueue(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main12)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        emailText = findViewById(R.id.emailText)
        phoneText = findViewById(R.id.phoneText)

        val userId = intent.getIntExtra("user_id", -1)

// Call API to fetch email and phone
        if (userId != -1) {
            getEmailByUserId(userId)
            getNumberByUserId(userId)
        }


        price = findViewById(R.id.price) // Assuming you have a TextView for price
        totalPayment = findViewById(R.id.totalPayment)
        tax = findViewById(R.id.tax)
        total = findViewById(R.id.total)

        username = findViewById(R.id.username)
        date = findViewById(R.id.date)

        val user = intent.getStringExtra("user")

        homePage = findViewById(R.id.homePage)
        homePage.setOnClickListener {
            val user = intent.getStringExtra("user")
            val userId = intent.getIntExtra("user_id", -1)
            val intent = Intent(this, MainActivity7::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("user", user)
            intent.putExtra("user_id", userId)
            startActivity(intent)
            finish()
        }

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("M-d-y", Locale.getDefault())
        val recentDate = dateFormat.format(calendar.time)

        date.text = recentDate.toString()

        // Get total price from Intent
        val totalPrice = intent.getIntExtra("totalPrice", 0)

        // Display total price in the TextView
        price.text = "₱$totalPrice.00"
        totalPayment.text = "₱$totalPrice.00"
        tax.text = "₱25.00"
        username.text = "$user"

        val totalText = totalPrice + 25
        total.text = "₱$totalText.00"
    }


    private fun getNumberByUserId(userId: Int) {
        val url = "${Constants.URL_GET_NUMBER_BY_USER_ID}?user_id=$userId"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            try {
                val jsonObject = JSONObject(response)
                val error = jsonObject.getBoolean("error")
                if (!error) {
                    val phone = jsonObject.getString("phone")
                    phoneText.text = phone
                } else {
                    phoneText.text = "Phone number not found"
                }
            } catch (e: JSONException) {
                phoneText.text = "Error parsing phone data"
                e.printStackTrace()
            }
        }, { error ->
            phoneText.text = "Network error"
            error.printStackTrace()
        })

        requestQueue.add(stringRequest)
    }

    private fun getEmailByUserId(userId: Int) {
        val url = "${Constants.URL_GET_EMAIL_BY_USER_ID}?user_id=$userId"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            try {
                val jsonObject = JSONObject(response)
                if (!jsonObject.getBoolean("error")) {
                    val email = jsonObject.getString("email")
                    emailText.text = email
                } else {
                    emailText.text = "Email not found"
                }
            } catch (e: JSONException) {
                emailText.text = "Error parsing email data"
                e.printStackTrace()
            }
        }, { error ->
            emailText.text = "Network error"
            error.printStackTrace()
        })

        requestQueue.add(stringRequest)
    }

}
