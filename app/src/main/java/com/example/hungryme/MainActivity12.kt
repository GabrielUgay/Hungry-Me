package com.example.hungryme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity12 : AppCompatActivity() {

    lateinit var price: TextView
    lateinit var totalPayment: TextView
    lateinit var tax: TextView
    lateinit var total: TextView

    lateinit var homePage: ImageView
    lateinit var username: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main12)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        price = findViewById(R.id.price) // Assuming you have a TextView for price
        totalPayment = findViewById(R.id.totalPayment)
        tax = findViewById(R.id.tax)
        total = findViewById(R.id.total)

        username = findViewById(R.id.username)

        val user = intent.getStringExtra("user")

        homePage = findViewById(R.id.homePage)
        homePage.setOnClickListener {
            val user = intent.getStringExtra("user")
            val intent = Intent(this, MainActivity5::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }

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
}
