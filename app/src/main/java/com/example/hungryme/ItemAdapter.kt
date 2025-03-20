package com.example.hungryme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

interface OnItemQuantityChangeListener {
    fun onQuantityChanged(position: Int, quantity: Int)
}

class ItemAdapter(
    private val items: MutableList<Item>,
    private val itemList: List<Item>,
    private val cartItems: MutableList<Bundle>,
    private val quantityChangeListener: OnItemQuantityChangeListener,
    private val user: String?,
    private val context: Context,
    private val mainActivity: MainTest
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.itemName)
        val price: TextView = view.findViewById(R.id.itemPrice)
        val image: ImageView = view.findViewById(R.id.imageItem)
        val subtract1: ImageView = view.findViewById(R.id.subtract1)
        val add1: ImageView = view.findViewById(R.id.add1)
        val value1: TextView = view.findViewById(R.id.value1)
        val favoriteButton: ImageView = view.findViewById(R.id.favoriteButton)
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("FavoritesPrefs", Context.MODE_PRIVATE)

    init {
        if (!user.isNullOrEmpty()) {
            loadCachedFavorites()
            fetchUserId(context, user) { userId ->
                if (!userId.startsWith("Error") && userId != "No Username") {
                    fetchFavoriteProducts(userId, context)
                }
            }
        }
    }

    private fun loadCachedFavorites() {
        val cachedFavorites = sharedPreferences.getStringSet("favoriteProducts_$user", emptySet()) ?: emptySet()
        mainActivity.favoriteItems.clear()
        mainActivity.favoriteItems.addAll(cachedFavorites.map { it.toInt() })
        Log.d("ItemAdapter", "Loaded cached favorites: ${mainActivity.favoriteItems}")
    }

    private fun saveCachedFavorites() {
        with(sharedPreferences.edit()) {
            putStringSet("favoriteProducts_$user", mainActivity.favoriteItems.map { it.toString() }.toSet())
            apply()
        }
        Log.d("ItemAdapter", "Saved cached favorites: ${mainActivity.favoriteItems}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]

        holder.name.text = item.name
        holder.price.text = item.category

        val quantity = cartItems.find { it.getString("name") == item.name }?.getInt("quantity") ?: 0
        holder.value1.text = quantity.toString()

        holder.subtract1.setOnClickListener {
            val currentQuantity = cartItems.find { it.getString("name") == item.name }?.getInt("quantity") ?: 0
            if (currentQuantity > 0) {
                updateQuantity(item, currentQuantity - 1)
                holder.value1.text = (currentQuantity - 1).toString()
                quantityChangeListener.onQuantityChanged(position, currentQuantity - 1)
            } else {
                Toast.makeText(holder.itemView.context, "No items to be removed.", Toast.LENGTH_SHORT).show()
            }
        }

        holder.add1.setOnClickListener {
            val currentQuantity = cartItems.find { it.getString("name") == item.name }?.getInt("quantity") ?: 0
            if (currentQuantity < 10) {
                updateQuantity(item, currentQuantity + 1)
                holder.value1.text = (currentQuantity + 1).toString()
                quantityChangeListener.onQuantityChanged(position, currentQuantity + 1)
            } else {
                Toast.makeText(holder.itemView.context, "You reached the limit of 10 items.", Toast.LENGTH_SHORT).show()
            }
        }

        try {
            val resId = R.drawable::class.java.getField(item.file).getInt(null)
            holder.image.setImageResource(resId)
        } catch (e: Exception) {
            Log.e("ItemAdapter", "Image not found: ${item.file}, using fallback.", e)
            holder.image.setImageResource(R.drawable.pork)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ItemActivity::class.java)
            intent.putExtra("item_id", item.id)
            intent.putExtra("item_name", item.name)
            intent.putExtra("item_price", item.price)
            Log.d("ItemAdapter", "Passing item price: ${item.price}")
            intent.putExtra("item_stock", item.stock)
            intent.putExtra("item_category", item.category)
            intent.putExtra("item_file", item.file)
            intent.putExtra("item_restaurant", item.restaurant)
            intent.putExtra("user", user)
            holder.itemView.context.startActivity(intent)
        }

        holder.favoriteButton.setImageResource(
            if (mainActivity.favoriteItems.contains(item.id)) R.drawable.red_heart else R.drawable.heart
        )

        holder.favoriteButton.setOnClickListener {
            fetchUserId(holder.itemView.context, user) { userId ->
                if (userId.startsWith("Error") || userId == "No Username") {
                    Toast.makeText(holder.itemView.context, "Please log in to favorite items", Toast.LENGTH_SHORT).show()
                    return@fetchUserId
                }

                if (mainActivity.favoriteItems.contains(item.id)) {
                    removeFromFavorites(userId, item.id, item.restaurant, item.category, holder.itemView.context) { success ->
                        if (success) {
                            mainActivity.favoriteItems.remove(item.id)
                            saveCachedFavorites()
                            holder.favoriteButton.setImageResource(R.drawable.heart)
                            Toast.makeText(holder.itemView.context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                            if (mainActivity.isShowingFavorites) {
                                items.removeAt(position)
                                notifyItemRemoved(position)
                            } else {
                                notifyItemChanged(position)
                            }
                        }
                    }
                } else {
                    addToFavorites(userId, item.id, item.restaurant, item.category, holder.itemView.context) { success ->
                        if (success) {
                            mainActivity.favoriteItems.add(item.id)
                            saveCachedFavorites()
                            holder.favoriteButton.setImageResource(R.drawable.red_heart)
                            Toast.makeText(holder.itemView.context, "Added to favorites", Toast.LENGTH_SHORT).show()
                            notifyItemChanged(position)
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount() = itemList.size

    private fun addToFavorites(userId: String, productId: Int, restaurant: String, category: String, context: Context, callback: (Boolean) -> Unit) {
        val url = Constants.URL_ADD_TO_FAVORITES
        val stringRequest = object : StringRequest(Request.Method.POST, url,
            { response ->
                Log.d("AddToFavorites", "Raw response: $response")
                try {
                    val jsonObject = JSONObject(response)
                    val success = jsonObject.getBoolean("success")
                    if (success) {
                        callback(true)
                    } else {
                        val message = jsonObject.optString("message", "Failed to add to favorites")
                        Log.e("AddToFavorites", "Server message: $message")
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
                } catch (e: JSONException) {
                    Log.e("AddToFavorites", "JSON error: ${e.message}")
                    Toast.makeText(context, "Error adding to favorites", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            },
            { error ->
                Log.e("AddToFavorites", "Network error: ${error.message}")
                error.networkResponse?.let {
                    Log.e("AddToFavorites", "Status code: ${it.statusCode}, Data: ${String(it.data)}")
                }
                Toast.makeText(context, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId
                params["product_id"] = productId.toString()
                params["restaurant"] = restaurant
                params["category"] = category
                Log.d("AddToFavorites", "Params: $params")
                return params
            }
        }
        Volley.newRequestQueue(context).add(stringRequest)
    }

    private fun removeFromFavorites(userId: String, productId: Int, restaurant: String, category: String, context: Context, callback: (Boolean) -> Unit) {
        val url = Constants.URL_REMOVE_FROM_FAVORITES
        val stringRequest = object : StringRequest(Request.Method.POST, url,
            { response ->
                Log.d("RemoveFromFavorites", "Raw response: $response")
                try {
                    val jsonObject = JSONObject(response)
                    val success = jsonObject.getBoolean("success")
                    if (success) {
                        callback(true)
                    } else {
                        val message = jsonObject.optString("message", "Failed to remove from favorites")
                        Log.e("RemoveFromFavorites", "Server message: $message")
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
                } catch (e: JSONException) {
                    Log.e("RemoveFromFavorites", "JSON error: ${e.message}")
                    Toast.makeText(context, "Error removing from favorites", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            },
            { error ->
                Log.e("RemoveFromFavorites", "Network error: ${error.message}")
                error.networkResponse?.let {
                    Log.e("RemoveFromFavorites", "Status code: ${it.statusCode}, Data: ${String(it.data)}")
                }
                Toast.makeText(context, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId
                params["product_id"] = productId.toString()
                params["restaurant"] = restaurant
                params["category"] = category
                Log.d("RemoveFromFavorites", "Params: $params")
                return params
            }
        }
        Volley.newRequestQueue(context).add(stringRequest)
    }

    private fun updateQuantity(item: Item, newQuantity: Int) {
        val existingItem = cartItems.find { it.getString("name") == item.name }
        if (existingItem != null) {
            existingItem.putInt("quantity", newQuantity)
        } else {
            val bundle = Bundle()
            bundle.putString("name", item.name)
            bundle.putInt("quantity", newQuantity)
            bundle.putDouble("price", item.price)
            cartItems.add(bundle)
        }
    }

    private fun fetchFavoriteProducts(userId: String, context: Context) {
        val url = "${Constants.URL_GET_FAVORITES}?user_id=$userId"
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getBoolean("success")) {
                        val favoritesArray = jsonObject.getJSONArray("favorites")
                        mainActivity.favoriteItems.clear()
                        for (i in 0 until favoritesArray.length()) {
                            val favorite = favoritesArray.getJSONObject(i)
                            mainActivity.favoriteItems.add(favorite.getInt("product_id"))
                        }
                        saveCachedFavorites()
                        Log.d("ItemAdapter", "Favorites fetched from server: ${mainActivity.favoriteItems}")
                        notifyDataSetChanged()
                    }
                } catch (e: JSONException) {
                    Log.e("ItemAdapter", "Error fetching favorites: ${e.message}")
                }
            },
            { error ->
                Log.e("ItemAdapter", "Network error fetching favorites: ${error.message}")
            })
        Volley.newRequestQueue(context).add(stringRequest)
    }

    private fun fetchUserId(context: Context, username: String?, callback: (String) -> Unit) {
        Log.d("fetchUserId", "Fetching user ID for username: $username")
        if (username.isNullOrEmpty()) {
            Log.e("fetchUserId", "Username is null or empty")
            callback("No Username")
            return
        }

        val url = "${Constants.URL_GET_USER_ID}?username=$username"
        Log.d("fetchUserId", "Request URL: $url")

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            Log.d("fetchUserId", "Raw response: $response")
            try {
                val jsonObject = JSONObject(response)
                Log.d("fetchUserId", "Parsed JSON: $jsonObject")
                if (jsonObject.getBoolean("success")) {
                    val userId = jsonObject.getString("user_id")
                    Log.d("fetchUserId", "Fetched user ID: $userId")
                    callback(userId)
                } else {
                    val message = jsonObject.optString("message", "Unknown error from server")
                    Log.e("fetchUserId", "Server error: $message")
                    callback("Server Error: $message")
                }
            } catch (e: JSONException) {
                Log.e("fetchUserId", "JSON parsing failed: ${e.message}")
                callback("JSON Error")
            }
        }, { error ->
            Log.e("fetchUserId", "Network error: ${error.message}")
            if (error.networkResponse != null) {
                Log.e("fetchUserId", "Status code: ${error.networkResponse.statusCode}")
                Log.e("fetchUserId", "Response data: ${String(error.networkResponse.data)}")
            }
            callback("Network Error")
        })

        Volley.newRequestQueue(context).add(stringRequest)
    }
}