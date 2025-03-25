package com.example.hungryme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity7 : AppCompatActivity() {
    lateinit var mangInasal: LinearLayout
    lateinit var jollibee: LinearLayout
    lateinit var greenwichLinearLayout: LinearLayout

    lateinit var mangInasalCart: FrameLayout
    lateinit var jollibeeCart: FrameLayout
    lateinit var greenwichCart: FrameLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main7)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Choose any restaurants here:
        mangInasal = findViewById(R.id.mangInasal)
        jollibee = findViewById(R.id.jollibee)
        greenwichLinearLayout = findViewById(R.id.greenwichLinearLayout)

        mangInasalCart = findViewById(R.id.mangInasalCart)
        jollibeeCart = findViewById(R.id.jollibeeCart)
        greenwichCart = findViewById(R.id.greenwichCart)

        mangInasal.setOnClickListener { goToPageNine() }
        jollibee.setOnClickListener { goToPageTen() }
        greenwichLinearLayout.setOnClickListener { goToPageEleven() }

        val userId = intent.getIntExtra("user_id", -1)
        val user = intent.getStringExtra("user") ?: ""
        Toast.makeText(this, "User ID: $userId", Toast.LENGTH_SHORT).show()
        Log.d("MainActivity7", "Received user_id: $userId, user: $user")

        if (userId == -1) {
            Log.e("MainActivity7", "Invalid user_id received")
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
        }

        mangInasalCart.setOnClickListener { goToCartPage("Kainan ni Mang Peping") }
        jollibeeCart.setOnClickListener { goToCartPage("Matutina") }
        greenwichCart.setOnClickListener { goToCartPage("Cuidad Elmina") }
    }

    private fun goToCartPage(restaurant: String) {
        val user = intent.getStringExtra("user") ?: ""
        val userId = intent.getIntExtra("user_id", -1)

        if (userId == -1) {
            Log.e("MainActivity7", "Invalid user_id for cart: $userId")
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, CartActivity::class.java).apply {
            putExtra("user", user)
            putExtra("restaurant", restaurant)
            putExtra("user_id", userId)
        }
        Log.d("MainActivity7", "Going to CartActivity with user_id: $userId, user: $user, restaurant: $restaurant")
        startActivity(intent)
    }

    private fun goToPageNine() {
        val user = intent.getStringExtra("user") ?: ""
        val userId = intent.getIntExtra("user_id", -1)
        val intent = Intent(this, MainTest::class.java)
        intent.putExtra("restaurant", "Kainan ni Mang Peping")
        intent.putExtra("user", user)
        intent.putExtra("user_id", userId)
        startActivity(intent)
    }

    private fun goToPageTen() {
        val user = intent.getStringExtra("user") ?: ""
        val userId = intent.getIntExtra("user_id", -1)
        val intent = Intent(this, MainTest::class.java)
        intent.putExtra("restaurant", "Matutina")
        intent.putExtra("user", user)
        intent.putExtra("user_id", userId)
        startActivity(intent)
    }

    private fun goToPageEleven() {
        val user = intent.getStringExtra("user") ?: ""
        val userId = intent.getIntExtra("user_id", -1)
        val intent = Intent(this, MainTest::class.java)
        intent.putExtra("restaurant", "Cuidad Elmina")
        intent.putExtra("user", user)
        intent.putExtra("user_id", userId)
        startActivity(intent)
    }
}