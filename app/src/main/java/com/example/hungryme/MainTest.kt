package com.example.hungryme

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class MainTest : AppCompatActivity(), OnItemQuantityChangeListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var checkOrder: FrameLayout
    private lateinit var checkOut: Button

    private lateinit var backPage: ImageView
    private lateinit var addCart: ImageView

    private lateinit var pushDown: ImageView
    private val itemList = mutableListOf<Item>()

    private var itemPrices = mutableListOf<Double>()
    private var itemCounts = mutableListOf<Int>()


    private fun updateTotalPrice() {
        val totalPrice = itemCounts.indices.sumOf { itemCounts[it] * itemPrices[it] }
        findViewById<TextView>(R.id.total).text = "₱$totalPrice" + "0"
    }



    private fun updateFoodSelection(index: Int, newValue: Int) {
        val foodSelectionLayout = findViewById<ViewGroup>(R.id.foodSelectionLayout)
        if (index !in itemList.indices) return

        if (newValue == 0) {
            val itemButton = foodSelectionLayout.findViewWithTag<Button>("item$index")
            itemButton?.let { foodSelectionLayout.removeView(it) }
        } else {
            var itemButton = foodSelectionLayout.findViewWithTag<Button>("item$index")
            if (itemButton == null) {
                itemButton = Button(this).apply {
                    text = itemList[index].name
                    tag = "item$index"
                    setOnClickListener { /* Handle click */ }
                }
                foodSelectionLayout.addView(itemButton)
            }
        }
    }


    private fun fetchUserId(username: String, callback: (Int) -> Unit) {
        val url = "${Constants.URL_GET_USER_ID}?username=$username"
        Log.d("DEBUG", "Fetching user ID from: $url")

        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                try {
                    Log.d("DEBUG", "Response from API: $response") // Add this

                    if (!response.getBoolean("error")) {
                        val userId = response.getInt("id")
                        Log.d("DEBUG", "User ID fetched: $userId") // Add this
                        callback(userId)
                    } else {
                        Log.e("ERROR", "Failed to get user ID")
                        callback(-1)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    callback(-1)
                }
            },
            Response.ErrorListener { error ->
                Log.e("API_ERROR", "Volley Error: ${error.message}")
                callback(-1)
            }
        )

        requestQueue.add(jsonObjectRequest)
    }




    private fun fetchStamps(userId: Int, restaurant: String) {
        val url = "${Constants.URL_GET_STAMPS}?user_id=$userId&restaurant=$restaurant"
        Log.d("DEBUG", "Fetching stamp count from: $url")

        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                try {
                    Log.d("DEBUG", "Response from API: $response") // Add this

                    if (!response.getBoolean("error")) {
                        val stampCount = response.getInt("count") // Get number of orders
                        Log.d("DEBUG", "Stamp count fetched: $stampCount") // Add this
                        updateStampBoxes(stampCount)
                    } else {
                        Log.e("ERROR", "Failed to get stamp count")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Log.e("API_ERROR", "Volley Error: ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }




    private fun updateStampBoxes(stampCount: Int) {
        Log.d("DEBUG", "Updating UI with stamp count: $stampCount")

        val boxes = listOf(
            R.id.box1, R.id.box2, R.id.box3, R.id.box4,
            R.id.box5, R.id.box6, R.id.box7, R.id.box8,
            R.id.box9, R.id.box10
        )

        runOnUiThread {
            for (i in boxes.indices) {
                val box = findViewById<ImageView>(boxes[i])
                if (box != null) {
                    if (i < stampCount) {
                        Log.d("DEBUG", "Setting box ${i + 1} to BLACK")
                        box.setImageResource(R.drawable.black_comp)
                    } else {
                        Log.d("DEBUG", "Setting box ${i + 1} to WHITE")
                        box.setImageResource(R.drawable.white_comp)
                    }
                } else {
                    Log.e("ERROR", "Box ${i + 1} not found in layout")
                }
            }
        }
    }






    override fun onQuantityChanged(position: Int, quantity: Int) {
        itemCounts[position] = quantity
        updateTotalPrice()
        updateFoodSelection(position, quantity)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_test)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val restaurant = intent.getStringExtra("restaurant") ?: "Default Restaurant"
        val user = intent.getStringExtra("user") ?: ""

        if (user.isNotEmpty()) {
            fetchUserId(user) { userId ->
                if (userId != -1) {
                    fetchStamps(userId, restaurant)
                } else {
                    Log.e("ERROR", "Failed to fetch user ID")
                }
            }
        }

        checkOrder = findViewById(R.id.checkOrder)
        checkOut = findViewById(R.id.checkOut)
        pushDown = findViewById(R.id.pushDown)
        backPage = findViewById(R.id.backPage)

        addCart = findViewById(R.id.addCart)

        addCart.setOnClickListener {
            val url = "${Constants.URL_GET_USER_ID}?username=$user"

            val requestQueue = Volley.newRequestQueue(this)

            val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    if (response.getBoolean("success")) {
                        val userId = response.getInt("user_id")

                        // First, delete cart items
                        deleteCart(userId, restaurant, requestQueue) {

                            // THIS IS NOT FUNCTIONING PROPERLY, LET ME SEND YOU THE ADD_CART_ITEMS
                            addCartItems(userId, restaurant, requestQueue) {
                                // After adding, navigate to MainActivity13
                                val cartItems = ArrayList<Bundle>()
                                for (i in itemCounts.indices) {
                                    if (itemCounts[i] > 0) {
                                        val bundle = Bundle().apply {
                                            putInt("id", itemList[i].id)
                                            putString("name", itemList[i].name)
                                            putInt("quantity", itemCounts[i])
                                            putDouble("price", itemPrices[i])
                                            putString("restaurant", restaurant)
                                            putString("file", itemList[i].file)
                                        }
                                        cartItems.add(bundle)
                                    }
                                }

                                val intent = Intent(this, MainActivity13::class.java)
                                intent.putParcelableArrayListExtra("cartItems", cartItems)
                                intent.putExtra("totalPrice", itemCounts.indices.sumOf { itemCounts[it] * itemPrices[it] }.toInt())
                                intent.putExtra("restaurant", restaurant)
                                intent.putExtra("user", user)
                                intent.putExtra("userId", userId)
                                startActivity(intent)
                            }
                        }
                    } else {
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                })

            requestQueue.add(jsonObjectRequest)
        }

        checkOut.setOnClickListener {
            toggleOrderView(true)
        }

        pushDown.setOnClickListener {
            toggleOrderView(false)
        }

        backPage.setOnClickListener {
            val user = intent.getStringExtra("user")
            val intent = Intent(this, MainActivity7::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns

        fetchItems(restaurant)

    }

    private fun toggleOrderView(show: Boolean) {
        checkOrder.post {
            val height = checkOrder.height.toFloat()
            val animator = ObjectAnimator.ofFloat(
                checkOrder, "translationY", if (show) height else 0f, if (show) 0f else height
            )
            animator.duration = 300
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    checkOrder.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
            checkOrder.visibility = View.VISIBLE
            animator.start()
        }
    }


    private fun deleteCart(userId: Int, restaurant: String, requestQueue: RequestQueue, callback: () -> Unit) {
        val deleteUrl = Constants.URL_DELETE_CART
        val deleteRequest = object : StringRequest(Method.POST, deleteUrl,
            { _ ->
                Log.d("DeleteCart", "Cart deleted successfully")
                callback()
            },
            { error ->
                Toast.makeText(this, "Error deleting cart: ${error.message}", Toast.LENGTH_SHORT).show()
                callback() // Still proceed to avoid app freezing
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("user_id" to userId.toString(), "restaurant" to restaurant)
            }
        }
        requestQueue.add(deleteRequest)
    }

    private fun addCartItems(userId: Int, restaurant: String, requestQueue: RequestQueue, callback: () -> Unit) {
        var pendingRequests = itemCounts.count { it > 0 } // Count items being added

        if (pendingRequests == 0) {
            callback() // No items to add, proceed
            return
        }

        for (i in itemCounts.indices) {
            if (itemCounts[i] > 0) {
                val addCartUrl = Constants.URL_ADD_CART
                val params = hashMapOf(
                    "user_id" to userId.toString(),
                    "product_id" to itemList[i].id.toString(),
                    "quantity" to itemCounts[i].toString(),
                    "restaurant" to restaurant
                )

                val addCartRequest = object : StringRequest(Method.POST, addCartUrl,
                    { response ->
                        Log.d("AddCartResponse", "Response: $response") // Log response
                        pendingRequests--
                        if (pendingRequests == 0) {
                            callback() // Proceed after all requests complete
                        }
                    },
                    { error ->
                        Log.e("AddCartError", "Error adding to cart: ${error.message}") // Log error
                        Toast.makeText(this, "Error adding to cart: ${error.message}", Toast.LENGTH_SHORT).show()
                        pendingRequests--
                        if (pendingRequests == 0) {
                            callback() // Proceed even if errors occur
                        }
                    }) {
                    override fun getParams(): MutableMap<String, String> = params
                }

                requestQueue.add(addCartRequest)
                Log.d("AddCartRequest", "Sending request: $params") // Log request
            }
        }
    }


    // In here kasi,  the goal is to use this SELECT id, name, price, stock, category FROM products WHERE restaurant = ?;
    private fun fetchItems(restaurantName: String) {
        val url = "${Constants.URL_FETCH_PRODUCTS}?restaurant=${restaurantName}"
        Log.d("API_URL", "Fetching from: $url")

        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                try {
                    Log.d("API_RESPONSE", "Response: $response")

                    if (!response.getBoolean("error")) {
                        val jsonArray = response.getJSONArray("items")
                        itemList.clear()
                        itemPrices.clear()
                        itemCounts.clear()

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val item = Item(
                                jsonObject.getInt("id"),
                                jsonObject.getString("name"),
                                jsonObject.getDouble("price"),
                                jsonObject.getInt("stock"),
                                jsonObject.getString("category"),
                                jsonObject.getString("restaurant"),
                                jsonObject.getString("file")
                            )
                            itemList.add(item)
                            itemPrices.add(jsonObject.getDouble("price"))
                            itemCounts.add(0)
                        }

                        itemAdapter = ItemAdapter(itemList, mutableListOf(), this) // OK ITS JUST ONE ERROR, FIX THAT ONE ERROR NOW!!!!!!!!!
                        recyclerView.adapter = itemAdapter
                    } else {
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Log.e("API_ERROR", "Volley Error: ${error.message}")
                Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(jsonObjectRequest)
    }



}
