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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CartActivity : AppCompatActivity(), OnItemQuantityChangeListener {

    private lateinit var orderListLayout: LinearLayout
    private lateinit var totalTextView: TextView
    private var cartItems: MutableList<Bundle> = mutableListOf()
    private var totalPrice = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart)

        orderListLayout = findViewById(R.id.orderListLayout)
        totalTextView = findViewById(R.id.totalTextView)

        val user = intent.getStringExtra("user") ?: ""
        val userId = intent.getIntExtra("user_id", -1)
        val restaurant = intent.getStringExtra("restaurant") ?: ""

        Log.d("CartActivity", "Received User: $user, UserID: $userId, Restaurant: $restaurant")
        Toast.makeText(this, "$userId Cart", Toast.LENGTH_SHORT).show()

        if (userId != -1) {
            fetchCartData(userId, restaurant)
        } else {
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.closeCart).setOnClickListener {
            finish()
        }

        findViewById<FrameLayout>(R.id.proceedToCheckOut).setOnClickListener {
            if (totalPrice <= 0) {
                Toast.makeText(this, "Cart is empty.", Toast.LENGTH_SHORT).show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Proceed to Order:")
                    .setMessage("Your total: ₱$totalPrice.00\nDo you want to proceed?")
                    .setPositiveButton("Yes") { _, _ ->
                        val requestQueue = Volley.newRequestQueue(this)
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
                                        // New request to add items to Order_Items
                                        val addOrderItemsRequest = object : StringRequest(Method.POST, Constants.URL_ADD_ORDER_ITEMS,
                                            Response.Listener { itemsResponse ->
                                                Log.d("AddOrderItemsRawResponse", "Raw response: '$itemsResponse'")
                                                try {
                                                    val itemsJson = JSONObject(itemsResponse)
                                                    Log.d("AddOrderItemsParsedJSON", "Parsed JSON: ${itemsJson.toString()}")
                                                    if (itemsJson.getBoolean("success")) {
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
                                                                                                            putExtra("user_id", userId)
                                                                                                        })
                                                                                                    } else {
                                                                                                        Toast.makeText(this, "Cart deletion failed: ${deleteJson.getString("message")}", Toast.LENGTH_SHORT).show()
                                                                                                    }
                                                                                                } catch (e: JSONException) {
                                                                                                    Log.e("JSONParsingError", "Error parsing deleteCartResponse: ${e.message}")
                                                                                                    Toast.makeText(this, "JSON Parsing Error $userId, $orderId lakdjf", Toast.LENGTH_SHORT).show()
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
                                                                                        requestQueue.add(deleteCartRequest)
                                                                                    } else {
                                                                                        Toast.makeText(this, "Receipt failed: ${receiptJson.getString("message")}", Toast.LENGTH_SHORT).show()
                                                                                    }
                                                                                } catch (e: JSONException) {
                                                                                    Log.e("JSONParsingError", "Error parsing addReceiptResponse: ${e.message}")
                                                                                    Toast.makeText(this, "JSON Parsing Error ok $orderId, $userId", Toast.LENGTH_SHORT).show()
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
                                                                        requestQueue.add(addReceiptRequest)
                                                                    } else {
                                                                        Toast.makeText(this, "Stock update failed: ${stockJson.getString("message")}", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                } catch (e: JSONException) {
                                                                    Log.e("JSONParsingError", "Error parsing updateStockResponse: ${e.message}")
                                                                    Toast.makeText(this, "JSON Parsing Error $orderId, again $userId", Toast.LENGTH_SHORT).show()
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
                                                        requestQueue.add(updateStockRequest)
                                                    } else {
                                                        Toast.makeText(this, "Order items failed: ${itemsJson.getString("message")}", Toast.LENGTH_SHORT).show()
                                                    }
                                                } catch (e: JSONException) {
                                                    Log.e("JSONParsingError", "Error parsing addOrderItemsResponse: ${e.message}, Raw response: '$itemsResponse'")
                                                    Toast.makeText(this, "JSON Parsing Error $orderId 4 $userId", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            Response.ErrorListener { error ->
                                                error.networkResponse?.data?.let {
                                                    val errorResponse = String(it, Charsets.UTF_8)
                                                    Log.e("AddOrderItemsError", "Network error: $errorResponse")
                                                }
                                                Log.e("AddOrderItemsError", "Volley error: ${error.message}")
                                                Toast.makeText(this, "Error adding order items: ${error.message}", Toast.LENGTH_SHORT).show()
                                            }) {
                                            override fun getParams(): Map<String, String> {
                                                val itemsJsonArray = JSONArray().apply {
                                                    cartItems.forEach { item ->
                                                        put(JSONObject().apply {
                                                            put("name", item.getString("name") ?: "Unknown")
                                                            put("quantity", item.getInt("quantity"))
                                                            put("price", item.getDouble("price"))
                                                        })
                                                    }
                                                }
                                                Log.d("AddOrderItemsParams", "Sending order_id: $orderId, user_id: $userId, cart_items: $itemsJsonArray")
                                                return mapOf(
                                                    "order_id" to orderId.toString(),
                                                    "user_id" to userId.toString(), // Added user_id here
                                                    "cart_items" to itemsJsonArray.toString()
                                                )
                                            }
                                        }
                                        requestQueue.add(addOrderItemsRequest)
                                    } else {
                                        Toast.makeText(this, "Order failed: ${orderJson.getString("message")}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: JSONException) {
                                    Log.e("JSONParsingError", "Error parsing addOrderResponse: ${e.message}")
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
                        requestQueue.add(addOrderRequest)
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
                                putInt("id", item.optInt("id")) // Add product_id
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

    private fun updateCart(userId: Int, productId: Int, quantity: Int, index: Int, oldQuantity: Int) {
        if (productId == -1) {
            Log.e("UpdateCart", "Invalid product ID: $productId for user $userId")
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show()
            revertQuantity(index, oldQuantity) // Revert to old quantity
            return
        }

        val url = Constants.URL_UPDATE_CART_ITEM
        val requestQueue = Volley.newRequestQueue(this)

        val request = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                Log.d("UpdateCartResponse", "Raw Response: $response")
                if (response.isEmpty()) {
                    Log.e("UpdateCart", "Empty response from server")
                    Toast.makeText(this, "Server returned an empty response", Toast.LENGTH_SHORT).show()
                    revertQuantity(index, oldQuantity)
                    return@Listener
                }
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getBoolean("success")) {
                        Log.d("UpdateCart", "Quantity updated successfully to $quantity for product $productId")
                        updateCartUI() // Only refresh UI on success
                    } else {
                        val errorMessage = jsonObject.optString("message", "Unknown error")
                        Log.e("UpdateCart", "Failed: $errorMessage")
                        revertQuantity(index, oldQuantity)
                        Toast.makeText(this, "Failed to update quantity: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Log.e("UpdateCart", "JSON Parsing Error: ${e.message}")
                    revertQuantity(index, oldQuantity)
                    Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Log.e("UpdateCart", "Volley Error: ${error.message}")
                error.networkResponse?.data?.let {
                    val errorBody = String(it, Charsets.UTF_8)
                    Log.e("UpdateCart", "Error Body: $errorBody")
                }
                revertQuantity(index, oldQuantity)
                Toast.makeText(this, "Failed to update cart: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = mapOf(
                    "user_id" to userId.toString(),
                    "product_id" to productId.toString(),
                    "quantity" to quantity.toString()
                )
                Log.d("UpdateCartParams", "Params: $params")
                return params
            }
        }
        requestQueue.add(request)
    }

    private fun removeItemFromCart(userId: Int, productId: Int, index: Int) {
        if (productId == -1) {
            Log.e("RemoveItem", "Invalid product ID: $productId for user $userId")
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show()
            revertQuantity(index, 1) // Revert to 1 if removal fails
            return
        }

        val url = Constants.URL_DELETE_ITEM_FROM_CART
        val requestQueue = Volley.newRequestQueue(this)

        val request = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                Log.d("RemoveItemResponse", "Raw Response: $response")
                if (response.isEmpty()) {
                    Log.e("RemoveItem", "Empty response from server")
                    Toast.makeText(this, "Server returned an empty response", Toast.LENGTH_SHORT).show()
                    revertQuantity(index, 1) // Revert to 1 if removal fails
                    return@Listener
                }
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getBoolean("success")) {
                        val itemName = cartItems[index].getString("name") ?: "Item"
                        Log.d("RemoveItem", "Successfully removed $itemName (ID: $productId)")
                        cartItems.removeAt(index)
                        updateCartUI()
                        Toast.makeText(this, "$itemName removed from cart", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMessage = jsonObject.optString("message", "Unknown error")
                        Log.e("RemoveItem", "Failed: $errorMessage")
                        revertQuantity(index, 1) // Revert to 1 if removal fails
                        Toast.makeText(this, "Failed to remove item: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Log.e("RemoveItem", "JSON Parsing Error: ${e.message}")
                    revertQuantity(index, 1) // Revert to 1 if removal fails
                    Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Log.e("RemoveItem", "Volley Error: ${error.message}")
                error.networkResponse?.data?.let {
                    val errorBody = String(it, Charsets.UTF_8)
                    Log.e("RemoveItem", "Error Body: $errorBody")
                }
                revertQuantity(index, 1) // Revert to 1 if removal fails
                Toast.makeText(this, "Failed to remove item: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = mapOf(
                    "user_id" to userId.toString(),
                    "product_id" to productId.toString(),
                )
                Log.d("RemoveItemParams", "Params: $params")
                return params
            }
        }
        requestQueue.add(request)
    }

    private fun revertQuantity(index: Int, quantity: Int) {
        cartItems[index].putInt("quantity", quantity)
        updateCartUI()
    }

    private fun updateCartUI() {
        runOnUiThread {
            orderListLayout.removeAllViews()
            totalPrice = 0

            val userId = intent.getIntExtra("user_id", -1)

            for ((index, item) in cartItems.withIndex()) {
                val itemName = item.getString("name") ?: "Unknown"
                val itemQuantity = item.getInt("quantity", 0)
                val itemPrice = item.getDouble("price", 0.0)
                val itemFile = item.getString("file", "")

                val productId = item.getInt("id", -1)

                totalPrice += (itemQuantity * itemPrice).toInt()

                val itemView = layoutInflater.inflate(R.layout.item_order, orderListLayout, false)
                itemView.findViewById<TextView>(R.id.orderName).text = itemName
                itemView.findViewById<TextView>(R.id.value1).text = "$itemQuantity"
                itemView.findViewById<TextView>(R.id.orderPrice).text = "₱${(itemQuantity * itemPrice).toInt()}.00"

                val subtractButton = itemView.findViewById<FrameLayout>(R.id.subtract1)
                val addButton = itemView.findViewById<FrameLayout>(R.id.add1)

                subtractButton.setOnClickListener {
                    if (itemQuantity > 0) {
                        val oldQuantity = itemQuantity
                        val newQuantity = itemQuantity - 1
                        Log.d("SubtractButton", "Old Quantity: $oldQuantity, New Quantity: $newQuantity, Product ID: $productId")
                        item.putInt("quantity", newQuantity)
                        if (newQuantity == 0) {
                            removeItemFromCart(userId, productId, index)
                        } else {
                            updateCart(userId, productId, newQuantity, index, oldQuantity)
                        }
                    }
                }

                addButton.setOnClickListener {
                    if (itemQuantity < 10) {
                        val oldQuantity = itemQuantity
                        val newQuantity = itemQuantity + 1
                        Log.d("AddButton", "Old Quantity: $oldQuantity, New Quantity: $newQuantity, Product ID: $productId")
                        item.putInt("quantity", newQuantity)
                        updateCart(userId, productId, newQuantity, index, oldQuantity)
                    } else {
                        Toast.makeText(this, "Cannot add more than 10 of $itemName", Toast.LENGTH_SHORT).show()
                    }
                }

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