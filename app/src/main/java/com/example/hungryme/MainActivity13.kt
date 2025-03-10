package com.example.hungryme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class MainActivity13 : AppCompatActivity(), OnItemQuantityChangeListener {

    private lateinit var orderListLayout: LinearLayout
    private lateinit var totalTextView: TextView
    private var cartItems: MutableList<Bundle> = mutableListOf()
    private var totalPrice = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main13)

        orderListLayout = findViewById(R.id.orderListLayout)
        totalTextView = findViewById(R.id.totalTextView)

        val user = intent.getStringExtra("user")
        val userId = intent.getIntExtra("userId", 2)  // Default to -1 to catch errors
        val restaurant = intent.getStringExtra("restaurant") ?: ""

        Log.d("MainActivity13", "Received User: $user, UserID: $userId, Restaurant: $restaurant")

        fetchCartData(userId, restaurant)


        findViewById<ImageView>(R.id.closeCart).setOnClickListener {
            startActivity(Intent(this, MainActivity7::class.java).apply {
                putExtra("user", user)
            })
        }


        // val restaurant = intent.getStringExtra("restaurant") ?: "" -- this is already here, no need to declare this again

        // MainActivity13.kt proceedToCheckOut.setOnClickListener { ... }
        findViewById<FrameLayout>(R.id.proceedToCheckOut).setOnClickListener {
            if (totalPrice <= 0) {
                Toast.makeText(this, "Cart is empty.", Toast.LENGTH_SHORT).show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Proceed to Order:")
                    .setMessage("Your total: ₱$totalPrice.00\nDo you want to proceed?")
                    .setPositiveButton("Yes") { _, _ ->
                        val requestQueue = Volley.newRequestQueue(this)

                        // Step 1: Insert Order First
                        val addOrderRequest = object : StringRequest(Method.POST, Constants.URL_ADD_ORDER,
                            Response.Listener { orderResponse ->
                                try {
                                    Log.d("AddOrderRawResponse", orderResponse)

                                    if (orderResponse.isEmpty()) {
                                        Log.e("AddOrderError", "Empty response from server")
                                        Toast.makeText(this, "Server returned an empty response.", Toast.LENGTH_SHORT).show()
                                        return@Listener
                                    }

                                    val orderJson = JSONObject(orderResponse)
                                    Log.d("AddOrderParsedJSON", orderJson.toString())

                                    if (orderJson.getBoolean("success")) {
                                        val orderId = orderJson.getInt("order_id")

                                        // Step 2: Update Stock First
                                        val updateStockRequest = object : StringRequest(Method.POST, Constants.URL_UPDATE_STOCK,
                                            Response.Listener { stockResponse ->
                                                try {
                                                    Log.d("UpdateStockRawResponse", stockResponse)

                                                    if (stockResponse.isEmpty()) {
                                                        Log.e("UpdateStockError", "Empty response from server")
                                                        Toast.makeText(this, "Server returned an empty response.", Toast.LENGTH_SHORT).show()
                                                        return@Listener
                                                    }

                                                    val stockJson = JSONObject(stockResponse)
                                                    Log.d("UpdateStockParsedJSON", stockJson.toString())

                                                    if (stockJson.getBoolean("success")) {
                                                        // Step 3: Insert Receipt
                                                        val addReceiptRequest = object : StringRequest(Method.POST, Constants.URL_ADD_RECEIPT,
                                                            Response.Listener { receiptResponse ->
                                                                try {
                                                                    Log.d("AddReceiptRawResponse", receiptResponse)

                                                                    if (receiptResponse.isEmpty()) {
                                                                        Log.e("AddReceiptError", "Empty response from server")
                                                                        Toast.makeText(this, "Server returned an empty response.", Toast.LENGTH_SHORT).show()
                                                                        return@Listener
                                                                    }

                                                                    val receiptJson = JSONObject(receiptResponse)
                                                                    Log.d("AddReceiptParsedJSON", receiptJson.toString())

                                                                    if (receiptJson.getBoolean("success")) {
                                                                        // Step 4: Delete Cart Only After Everything is Successful
                                                                        val deleteCartRequest = object : StringRequest(Method.POST, Constants.URL_DELETE_CART,
                                                                            Response.Listener { deleteResponse ->
                                                                                try {
                                                                                    Log.d("DeleteCartRawResponse", deleteResponse)

                                                                                    if (deleteResponse.isEmpty()) {
                                                                                        Log.e("DeleteCartError", "Empty response from server")
                                                                                        Toast.makeText(this, "Server returned an empty response.", Toast.LENGTH_SHORT).show()
                                                                                        return@Listener
                                                                                    }

                                                                                    val deleteJson = JSONObject(deleteResponse)
                                                                                    Log.d("DeleteCartParsedJSON", deleteJson.toString())

                                                                                    if (deleteJson.getBoolean("success")) {
                                                                                        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                                                                                        startActivity(Intent(this, MainActivity12::class.java).apply {
                                                                                            putExtra("totalPrice", totalPrice)
                                                                                            putExtra("cartItems", ArrayList(cartItems))
                                                                                            putExtra("user", user)
                                                                                        })
                                                                                    } else {
                                                                                        Toast.makeText(this, "Cart deletion failed: ${deleteJson.getString("message")}", Toast.LENGTH_SHORT).show()
                                                                                    }
                                                                                } catch (e: JSONException) {
                                                                                    Log.e("JSONParsingError", "Error parsing deleteCartResponse", e)
                                                                                    Toast.makeText(this, "JSON Parsing Error", Toast.LENGTH_SHORT).show()
                                                                                }
                                                                            },
                                                                            Response.ErrorListener { error ->
                                                                                error.networkResponse?.data?.let {
                                                                                    val errorResponse = String(it, Charsets.UTF_8)
                                                                                    Log.e("DeleteCartError", errorResponse)
                                                                                }
                                                                                Toast.makeText(this, "Error deleting cart: ${error.message}", Toast.LENGTH_SHORT).show()
                                                                            }) {
                                                                            override fun getParams(): Map<String, String> {
                                                                                return mapOf(
                                                                                    "user_id" to userId.toString(),
                                                                                    "restaurant" to restaurant
                                                                                )
                                                                            }
                                                                        }

                                                                        requestQueue.add(deleteCartRequest) // Execute Delete Cart
                                                                    } else {
                                                                        Toast.makeText(this, "Receipt failed: ${receiptJson.getString("message")}", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                } catch (e: JSONException) {
                                                                    Log.e("JSONParsingError", "Error parsing addReceiptResponse", e)
                                                                    Toast.makeText(this, "JSON Parsing Error", Toast.LENGTH_SHORT).show()
                                                                }
                                                            },
                                                            Response.ErrorListener { error ->
                                                                error.networkResponse?.data?.let {
                                                                    val errorResponse = String(it, Charsets.UTF_8)
                                                                    Log.e("AddReceiptError", errorResponse)
                                                                }
                                                                Toast.makeText(this, "Error adding receipt: ${error.message}", Toast.LENGTH_SHORT).show()
                                                            }) {
                                                            override fun getParams(): Map<String, String> {
                                                                return mapOf(
                                                                    "order_id" to orderId.toString(),
                                                                    "user_id" to userId.toString(),
                                                                    "total_amount" to totalPrice.toString()
                                                                )
                                                            }
                                                        }

                                                        requestQueue.add(addReceiptRequest) // Execute Add Receipt
                                                    } else {
                                                        Toast.makeText(this, "Stock update failed: ${stockJson.getString("message")}", Toast.LENGTH_SHORT).show()
                                                    }
                                                } catch (e: JSONException) {
                                                    Log.e("JSONParsingError", "Error parsing updateStockResponse", e)
                                                    Toast.makeText(this, "JSON Parsing Error", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            Response.ErrorListener { error ->
                                                error.networkResponse?.data?.let {
                                                    val errorResponse = String(it, Charsets.UTF_8)
                                                    Log.e("UpdateStockError", errorResponse)
                                                }
                                                Toast.makeText(this, "Error updating stock: ${error.message}", Toast.LENGTH_SHORT).show()
                                            }) {
                                            override fun getParams(): Map<String, String> {
                                                return mapOf(
                                                    "user_id" to userId.toString(),
                                                    "restaurant" to restaurant,
                                                    "cart_items" to cartItems.joinToString { it.toString() }
                                                )
                                            }
                                        }

                                        requestQueue.add(updateStockRequest) // Execute Stock Update

                                    } else {
                                        Toast.makeText(this, "Order failed: ${orderJson.getString("message")}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: JSONException) {
                                    Log.e("JSONParsingError", "Error parsing addOrderResponse", e)
                                    Toast.makeText(this, "JSON Parsing Error", Toast.LENGTH_SHORT).show()
                                }
                            },
                            Response.ErrorListener { error ->
                                error.networkResponse?.data?.let {
                                    val errorResponse = String(it, Charsets.UTF_8)
                                    Log.e("AddOrderError", errorResponse)
                                }
                                Toast.makeText(this, "Error placing order: ${error.message}", Toast.LENGTH_SHORT).show()
                            }) {
                            override fun getParams(): Map<String, String> {
                                return mapOf(
                                    "user_id" to userId.toString(),
                                    "total_price" to totalPrice.toString(),
                                    "restaurant" to restaurant
                                )
                            }
                        }

                        requestQueue.add(addOrderRequest) // Execute Add Order First
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }






    }

    private fun fetchCartData(userId: Int, restaurant: String) {
        val url = "${Constants.URL_FETCH_CART}?user_id=$userId&restaurant=$restaurant"
        Log.d("CartData", "Fetching from URL: $url")

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                Log.d("CartData", "Response: $response")
                try {
                    val jsonObject = JSONObject(response)
                    cartItems.clear()

                    if (jsonObject.has("items")) {
                        val itemsArray = jsonObject.getJSONArray("items")
                        for (i in 0 until itemsArray.length()) {
                            val item = itemsArray.getJSONObject(i)
                            cartItems.add(Bundle().apply {
                                putString("name", item.optString("name"))
                                putDouble("price", item.optDouble("price"))
                                putInt("quantity", item.optInt("quantity"))
                                putString("file", item.optString("file"))
                            })
                        }
                        updateCartUI()
                    } else {
                        Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Log.e("CartData", "JSON Parsing Error: ${e.message}")
                }
            },
            { error ->
                Log.e("CartData", "Volley Error: ${error.message}")
                Toast.makeText(this, "Failed to fetch cart data", Toast.LENGTH_SHORT).show()
            })

        Volley.newRequestQueue(this).add(request)
    }


    override fun onQuantityChanged(position: Int, quantity: Int) {
        cartItems[position].putInt("quantity", quantity)
        updateCartUI()
    }

    private fun updateCartUI() {
        runOnUiThread {
            orderListLayout.removeAllViews()
            totalPrice = 0

            for (item in cartItems) {
                val itemName = item.getString("name") ?: "Unknown"
                val itemQuantity = item.getInt("quantity", 0)
                val itemPrice = item.getDouble("price", 0.0)
                val itemFile = item.getString("file", "")

                totalPrice += (itemQuantity * itemPrice).toInt()

                val itemView = layoutInflater.inflate(R.layout.item_order, orderListLayout, false)

                itemView.findViewById<TextView>(R.id.orderName).text = itemName
                itemView.findViewById<TextView>(R.id.value1).text = "$itemQuantity"
                itemView.findViewById<TextView>(R.id.orderPrice).text = "₱${(itemQuantity * itemPrice).toInt()}.00"

                if (itemFile.isNotEmpty()) {
                    val resourceId = resources.getIdentifier(itemFile, "drawable", packageName)
                    if (resourceId != 0) {
                        itemView.findViewById<ImageView>(R.id.orderImage).setImageResource(resourceId)
                    } else {
                        Log.e("CartData", "Image not found: $itemFile")
                    }
                }

                orderListLayout.addView(itemView)
            }

            totalTextView.text = "Total: ₱$totalPrice.00"
        }
    }
}
