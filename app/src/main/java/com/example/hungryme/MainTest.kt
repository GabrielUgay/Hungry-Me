package com.example.hungryme

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class MainTest : AppCompatActivity(), OnItemQuantityChangeListener {
    private var lastSelectedButton: Button? = null // Store last clicked button
    private var lastSelectedCategory: String? = "All"
    private lateinit var searchBar: EditText

    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var checkOrder: FrameLayout
    private lateinit var checkOut: Button

    // I already set the lateinit vars here:
    private lateinit var all: Button
    private lateinit var dish: Button
    private lateinit var drinks: Button
    private lateinit var desserts: Button

    private lateinit var pushDown: ImageView
    private val itemList = mutableListOf<Item>()

    private var filteredItemList = mutableListOf<Item>()
    private var itemPrices = mutableListOf<Double>()
    private var itemCounts = mutableListOf<Int>()


    private fun filterItems(query: String) {
        filteredItemList.clear()

        if (query.isEmpty()) {
            filteredItemList.addAll(itemList) // Show all items if query is empty
        } else {
            val lowerCaseQuery = query.lowercase()
            for (item in itemList) {
                if (item.name.lowercase().startsWith(lowerCaseQuery)) {
                    filteredItemList.add(item)
                }
            }
        }

        itemAdapter.notifyDataSetChanged() // Refresh the RecyclerView
    }



    private fun updateTotalPrice() {
        val totalPrice = itemCounts.indices.sumOf { itemCounts[it] * itemPrices[it] }
        findViewById<TextView>(R.id.total).text = "₱$totalPrice" + "0"
    }


    private fun categoryOrder(category: String): Int {
        return when (category) {
            "All" -> 0
            "Dish" -> 1
            "Drinks" -> 2
            "Desserts" -> 3
            else -> 0
        }
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

    //asdf
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


    private fun resetButtonColors() {
        all.setBackgroundColor(Color.parseColor("#F3F4F6"))
        dish.setBackgroundColor(Color.parseColor("#F3F4F6"))

        drinks.setBackgroundColor(Color.parseColor("#F3F4F6"))
        desserts.setBackgroundColor(Color.parseColor("#F3F4F6"))

        all.setTextColor(Color.parseColor("#202020"))
        dish.setTextColor(Color.parseColor("#202020"))

        drinks.setTextColor(Color.parseColor("#202020"))
        desserts.setTextColor(Color.parseColor("#202020"))
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



    private fun fetchItemsByCategory(restaurantName: String, category: String) {
        val url = "${Constants.URL_FETCH_ITEMS_BY_CATEGORY}?restaurant=${restaurantName}&category=${category}"
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
                        filteredItemList.clear() // Make sure it's reset!

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

                        val user = intent.getStringExtra("user")

                        // ✅ Update filteredItemList to match itemList (just like in fetchItems)
                        filteredItemList.addAll(itemList)
                        itemAdapter = ItemAdapter(filteredItemList, mutableListOf(), this, user)
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

        // I already got the ids here
        all = findViewById(R.id.all)
        dish = findViewById(R.id.dish)
        drinks = findViewById(R.id.drinks)
        desserts = findViewById(R.id.desserts)


        searchBar = findViewById(R.id.searchBar)

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterItems(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        all.setOnClickListener {
            animateCategoryChange("All", all)
            fetchItems(restaurant)
        }

        dish.setOnClickListener {
            animateCategoryChange("Dish", dish)
            fetchItemsByCategory(restaurant, "Dish")
        }

        drinks.setOnClickListener {
            animateCategoryChange("Drinks", drinks)
            fetchItemsByCategory(restaurant, "Drinks")
        }

        desserts.setOnClickListener {
            animateCategoryChange("Desserts", desserts)
            fetchItemsByCategory(restaurant, "Desserts")
        }





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



        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns

        // I do have fetchItems() and fetchItemsByCategory()
        fetchItems(restaurant)

    }


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
                        filteredItemList.clear()

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

                        // Now, the goal is I want the product_id to go to itemAdapter
                        val user = intent.getStringExtra("user")
                        itemAdapter = ItemAdapter(itemList, mutableListOf(), this, user) // OK ITS JUST ONE ERROR, FIX THAT ONE ERROR NOW!!!!!!!!!
                        recyclerView.adapter = itemAdapter

                        filteredItemList.addAll(itemList) // Initially, show all items
                        itemAdapter = ItemAdapter(filteredItemList, mutableListOf(), this, user)
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


    private fun animateCategoryChange(newCategory: String, selectedButton: Button) {
        val container: View = findViewById(R.id.recyclerView) // Replace with your actual container ID

        // Define category order
        val categoryOrder = listOf("All", "Dish", "Drinks", "Desserts")

        // Get index of each category
        val lastIndex = categoryOrder.indexOf(lastSelectedCategory)
        val newIndex = categoryOrder.indexOf(newCategory)

        // Determine swipe direction
        val direction = when {
            newIndex > lastIndex -> R.anim.slide_to_right // Moving forward (right)
            newIndex < lastIndex -> R.anim.slide_to_left // Moving backward (left)
            else -> 0 // No change
        }

        // Run animation if needed
        if (direction != 0) {
            val animation = AnimationUtils.loadAnimation(this, direction)
            container.startAnimation(animation)
        }

        // Update button UI
        resetButtonColors()
        selectedButton.setBackgroundColor(Color.parseColor("#00FF00")) // Green
        selectedButton.setTextColor(Color.parseColor("#202020"))

        // Update last selected category
        lastSelectedCategory = newCategory
    }
}
