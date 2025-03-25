package com.example.hungryme

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ItemActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private var numberItems = 0
    private var pricePerItem = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        requestQueue = Volley.newRequestQueue(this)

        // Retrieve user_id as an Int from the intent
        val userId = intent.getIntExtra("user_id", -1)

        val itemName = findViewById<TextView>(R.id.itemName)
        val itemDesc = findViewById<TextView>(R.id.itemDesc)
        val imageFile = findViewById<ImageView>(R.id.imageFile)
        val portion = findViewById<TextView>(R.id.portion)
        val totalPrice = findViewById<TextView>(R.id.totalPrice)
        val minusGreen = findViewById<ImageView>(R.id.minusGreen)
        val plusGreen = findViewById<ImageView>(R.id.plusGreen)
        val addToCart = findViewById<FrameLayout>(R.id.addToCart)
        val backButton = findViewById<ImageView>(R.id.backButton)

        val itemId = intent.getIntExtra("item_id", 0)
        val itemFile = intent.getStringExtra("item_file")
        val restaurant = intent.getStringExtra("item_restaurant") ?: ""
        pricePerItem = intent.getDoubleExtra("item_price", 0.0)

        itemName.text = intent.getStringExtra("item_name") ?: "Unknown Item"
        itemDesc.text = intent.getStringExtra("item_desc") ?: "No description available."

        imageFile.setImageResource(resources.getIdentifier(itemFile, "drawable", packageName))

        updatePrice(portion, totalPrice)

        minusGreen.setOnClickListener {
            if (numberItems > 0) {
                numberItems--
                updatePrice(portion, totalPrice)
            }
        }

        plusGreen.setOnClickListener {
            if (numberItems < 10) {
                numberItems++
                updatePrice(portion, totalPrice)
            }
        }

        addToCart.setOnClickListener {
            if (userId == -1) {
                Log.e("ItemActivity", "Invalid user_id: $userId. Cannot add to cart.")
                Toast.makeText(this, "User not identified. Please log in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (numberItems <= 0) {
                Toast.makeText(this, "Quantity must be greater than 0.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addToCart(userId, itemId, numberItems, restaurant)
        }

        backButton.setOnClickListener { finish() }
    }

    private fun updatePrice(portion: TextView, totalPrice: TextView) {
        val total = numberItems * pricePerItem
        portion.text = numberItems.toString()
        totalPrice.text = "₱${String.format("%.2f", total)}"
    }

    private fun addToCart(userId: Int, productId: Int, quantity: Int, restaurant: String) {
        val url = Constants.URL_ADD_CART
        Log.d("ItemActivity", "Adding to cart: UserID=$userId, ProductID=$productId, Quantity=$quantity, Restaurant=$restaurant")

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        Log.d("ItemActivity", "Item successfully added to cart.")
                        Toast.makeText(this, "Item added to cart.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("ItemActivity", "Failed to add item: ${jsonResponse.getString("message")}")
                        Toast.makeText(this, "Failed to add item: ${jsonResponse.getString("message")}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("ItemActivity", "JSON Parsing Error: ${e.message}")
                    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("ItemActivity", "Volley Error: ${error.message}")
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "user_id" to userId.toString(),
                    "product_id" to productId.toString(),
                    "quantity" to quantity.toString(),
                    "restaurant" to restaurant
                )
            }
        }

        requestQueue.add(stringRequest)
    }
}