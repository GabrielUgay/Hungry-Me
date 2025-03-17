package com.example.hungryme

import android.content.Context
import android.content.Intent
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
    private val itemList: List<Item>,
    private val cartItems: MutableList<Bundle>,
    private val quantityChangeListener: OnItemQuantityChangeListener,
    private val user: String?
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

    private val favoriteProducts = mutableSetOf<Int>() // Store favorite product IDs

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]

        // Set the item name immediately as a default
        holder.name.text = item.name
        holder.price.text = item.category

        // Fetch the user ID and append it to the name when available
        fetchUserId(holder.itemView.context, user) { userId ->
            checkFavoriteStatus(holder.itemView.context, userId.toInt()) { favorites ->
                favoriteProducts.clear()
                favoriteProducts.addAll(favorites)

                updateHeartColor(holder.favoriteButton, item.id)

                holder.favoriteButton.setOnClickListener {
                    if (favoriteProducts.contains(item.id)) {
                        removeFromFavorites(holder.itemView.context, userId.toInt(), item.id) {
                            favoriteProducts.remove(item.id)
                            updateHeartColor(holder.favoriteButton, item.id)
                            Toast.makeText(holder.itemView.context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        addToFavorites(holder.itemView.context, userId.toInt(), item.id) {
                            favoriteProducts.add(item.id)
                            updateHeartColor(holder.favoriteButton, item.id)
                            Toast.makeText(holder.itemView.context, "Added to favorites", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // Handle quantity display and updates
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

        // Set the image
        try {
            val resId = R.drawable::class.java.getField(item.file).getInt(null)
            holder.image.setImageResource(resId)
        } catch (e: Exception) {
            Log.e("ItemAdapter", "Image not found: ${item.file}, using fallback.", e)
            holder.image.setImageResource(R.drawable.pork)
        }

        // Handle item click
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
    }

    override fun getItemCount() = itemList.size

    private fun updateHeartColor(button: ImageView, productId: Int) {
        button.setImageResource(if (favoriteProducts.contains(productId)) R.drawable.red_heart else R.drawable.heart)
    }

    private fun addToFavorites(context: Context, userId: Int, productId: Int, callback: () -> Unit) {
        val url = Constants.URL_ADD_FAVORITE
        val request = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                val jsonResponse = JSONObject(response)
                if (jsonResponse.getBoolean("success")) {
                    callback()
                }
            },
            Response.ErrorListener { error -> error.printStackTrace() }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("user_id" to userId.toString(), "product_id" to productId.toString())
            }
        }
        Volley.newRequestQueue(context).add(request)
    }

    private fun removeFromFavorites(context: Context, userId: Int, productId: Int, callback: () -> Unit) {
        val url = Constants.URL_REMOVE_FAVORITE
        val request = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                val jsonResponse = JSONObject(response)
                if (jsonResponse.getBoolean("success")) {
                    callback()
                }
            },
            Response.ErrorListener { error -> error.printStackTrace() }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("user_id" to userId.toString(), "product_id" to productId.toString())
            }
        }
        Volley.newRequestQueue(context).add(request)
    }

    private fun checkFavoriteStatus(context: Context, userId: Int, callback: (Set<Int>) -> Unit) {
        val url = "${Constants.URL_GET_FAVORITES}?user_id=$userId"
        val request = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                val jsonResponse = JSONObject(response)
                if (jsonResponse.getBoolean("success")) {
                    val favoritesArray = jsonResponse.getJSONArray("favorites")
                    val favoriteIds = mutableSetOf<Int>()
                    for (i in 0 until favoritesArray.length()) {
                        favoriteIds.add(favoritesArray.getInt(i))
                    }
                    callback(favoriteIds)
                } else {
                    callback(emptySet())
                }
            },
            Response.ErrorListener { error -> error.printStackTrace() }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("user_id" to userId.toString())
            }
        }
        Volley.newRequestQueue(context).add(request)
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