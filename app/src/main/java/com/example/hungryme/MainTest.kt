package com.example.hungryme

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
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
import org.json.JSONObject

class MainTest : AppCompatActivity(), OnItemQuantityChangeListener {
    private var lastSelectedButton: Button? = null
    private var lastSelectedCategory: String? = "All"
    private lateinit var searchBar: EditText

    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var checkOrder: FrameLayout

    private lateinit var all: Button
    private lateinit var dish: Button
    private lateinit var drinks: Button
    private lateinit var desserts: Button

    private lateinit var homePage2: ImageView
    private lateinit var favs: ImageView

    private lateinit var pushDown: ImageView
    private val itemList = mutableListOf<Item>()
    private var filteredItemList = mutableListOf<Item>()
    private var itemPrices = mutableListOf<Double>()
    private var itemCounts = mutableListOf<Int>()

    var favoriteItems = mutableSetOf<Int>()
    var isShowingFavorites = false

    private val cartItems = mutableListOf<Bundle>()

    private fun filterItems(query: String) {
        filteredItemList.clear()
        if (query.isEmpty()) {
            filteredItemList.addAll(itemList)
        } else {
            val lowerCaseQuery = query.lowercase()
            for (item in itemList) {
                if (item.name.lowercase().startsWith(lowerCaseQuery)) {
                    filteredItemList.add(item)
                }
            }
        }
        itemAdapter.notifyDataSetChanged()
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
                    Log.d("DEBUG", "Response from API: $response")
                    if (!response.getBoolean("error")) {
                        val stampCount = response.getInt("count")
                        Log.d("DEBUG", "Stamp count fetched: $stampCount")
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
                        box.setImageResource(R.drawable.black_comp)
                    } else {
                        box.setImageResource(R.drawable.white_comp)
                    }
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

                        if (isShowingFavorites) {
                            filterFavorites(category)
                        } else {
                            filteredItemList.addAll(itemList)
                            itemAdapter.notifyDataSetChanged()
                        }
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
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun goToCartPage(restaurant: String) {
        val user = intent.getStringExtra("user") ?: ""
        val userId = intent.getIntExtra("user_id", -1)

        if (userId == -1) {
            Log.e("MainTest", "Invalid user_id: $userId")
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, CartActivity::class.java).apply {
            putExtra("user", user)
            putExtra("restaurant", restaurant)
            putExtra("user_id", userId)
        }
        Log.d("MainTest", "Going to CartActivity with user_id: $userId, user: $user")
        startActivity(intent)
    }

    private fun filterFavorites(category: String) {
        filteredItemList.clear()
        val itemsToShow = if (category == "All") {
            itemList.filter { favoriteItems.contains(it.id) }
        } else {
            itemList.filter { favoriteItems.contains(it.id) && it.category == category }
        }
        filteredItemList.addAll(itemsToShow)
        itemAdapter.notifyDataSetChanged()
        if (filteredItemList.isEmpty()) {
            Toast.makeText(this, "No favorite items in this category", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleFavorite(itemId: Int) {
        if (favoriteItems.contains(itemId)) {
            favoriteItems.remove(itemId)
            if (isShowingFavorites) {
                val itemToRemove = filteredItemList.find { it.id == itemId }
                itemToRemove?.let {
                    filteredItemList.remove(it)
                    itemAdapter.notifyDataSetChanged()
                }
            }
        } else {
            favoriteItems.add(itemId)
        }
    }

    // In MainTest.kt, update the ItemAdapter initialization in onCreate
    @SuppressLint("MissingInflatedId")
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
        val userId = intent.getIntExtra("user_id", -1)
        Log.d("MainTest", "Initial values - user_id: $userId, user: $user, restaurant: $restaurant")

        Toast.makeText(this, "$userId Yehey!", Toast.LENGTH_SHORT).show()

        if (userId != -1) {
            fetchStamps(userId, restaurant)
        }

        all = findViewById(R.id.all)
        dish = findViewById(R.id.dish)
        drinks = findViewById(R.id.drinks)
        desserts = findViewById(R.id.desserts)

        homePage2 = findViewById(R.id.homePage2)
        favs = findViewById(R.id.favs)

        val cartPage = findViewById<FrameLayout>(R.id.cartPage)
        cartPage.setOnClickListener {
            goToCartPage(restaurant)
        }

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

        homePage2.setOnClickListener {
            finish()
        }

        favs.setOnClickListener {
            if (!isShowingFavorites) {
                isShowingFavorites = true
                favs.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                filterFavorites(lastSelectedCategory ?: "All")
            } else {
                isShowingFavorites = false
                favs.clearColorFilter()
                when (lastSelectedCategory) {
                    "All" -> fetchItems(restaurant)
                    "Dish" -> fetchItemsByCategory(restaurant, "Dish")
                    "Drinks" -> fetchItemsByCategory(restaurant, "Drinks")
                    "Desserts" -> fetchItemsByCategory(restaurant, "Desserts")
                }
            }
        }

        checkOrder = findViewById(R.id.checkOrder)
        pushDown = findViewById(R.id.pushDown)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        filteredItemList.addAll(itemList)
        itemAdapter = ItemAdapter(filteredItemList, filteredItemList, cartItems, this, user, userId, this, this) // Pass userId here
        recyclerView.adapter = itemAdapter

        val recentOrders = findViewById<ImageView>(R.id.recentOrders)
        val profilePic = findViewById<ImageView>(R.id.profilePic)
        recentOrders.setOnClickListener {
            if (userId == -1) {
                Log.e("MainTest", "Invalid user_id for RecentOrders: $userId")
                Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, RecentOrders::class.java).apply {
                putExtra("user", user)
                putExtra("restaurant", restaurant)
                putExtra("user_id", userId)
            }
            Log.d("MainTest", "Going to RecentOrders with user_id: $userId, user: $user")
            startActivity(intent)
        }

        profilePic.setOnClickListener {
            if (userId == -1) {
                Log.e("MainTest", "Invalid user_id for ProfileActivity: $userId")
                Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("user", user)
                putExtra("restaurant", restaurant)
                putExtra("user_id", userId)
            }
            Log.d("MainTest", "Going to ProfileActivity with user_id: $userId, user: $user")
            startActivity(intent)
        }

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

                        if (isShowingFavorites) {
                            filterFavorites("All")
                        } else {
                            filteredItemList.addAll(itemList)
                            itemAdapter.notifyDataSetChanged()
                        }
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
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun animateCategoryChange(newCategory: String, selectedButton: Button) {
        val container: View = findViewById(R.id.recyclerView)
        val categoryOrder = listOf("All", "Dish", "Drinks", "Desserts")
        val lastIndex = categoryOrder.indexOf(lastSelectedCategory)
        val newIndex = categoryOrder.indexOf(newCategory)

        val direction = when {
            newIndex > lastIndex -> R.anim.slide_to_right
            newIndex < lastIndex -> R.anim.slide_to_left
            else -> 0
        }

        if (direction != 0) {
            val animation = AnimationUtils.loadAnimation(this, direction)
            container.startAnimation(animation)
        }

        resetButtonColors()
        selectedButton.setBackgroundColor(Color.parseColor("#00FF00"))
        selectedButton.setTextColor(Color.parseColor("#202020"))

        lastSelectedCategory = newCategory
    }
}