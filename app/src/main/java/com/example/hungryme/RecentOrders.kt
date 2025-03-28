package com.example.hungryme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentOrders : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_orders)

        recyclerView = findViewById(R.id.recentOrdersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        emptyStateText = findViewById(R.id.emptyStateText)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        val user = intent.getStringExtra("user")
        val userId = intent.getIntExtra("user_id", -1)
        val restaurant = intent.getStringExtra("restaurant") ?: "No restaurant indicated"

        if (userId == -1) {
            emptyStateText.text = "Invalid user ID"
            emptyStateText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }

        fetchOrders(userId.toString(), restaurant)
    }

    private fun fetchOrders(userId: String, restaurant: String) {
        val url = "${Constants.URL_FETCH_ORDERS}?user_id=$userId&restaurant=$restaurant"
        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                try {
                    Log.d("API_RESPONSE", response.toString()) // Debugging

                    if (response.getBoolean("error")) {
                        emptyStateText.text = response.getString("message")
                        emptyStateText.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        return@Listener
                    }

                    val ordersArray = response.getJSONArray("orders")
                    val ordersList = mutableListOf<Map<String, String>>()

                    for (i in 0 until ordersArray.length()) {
                        val orderJson = ordersArray.getJSONObject(i)
                        val order = mapOf(
                            "id" to orderJson.getString("id"),
                            "created_at" to orderJson.getString("created_at"),
                            "total_price" to orderJson.getString("total_price"),
                            "status" to orderJson.getString("status")
                        )
                        ordersList.add(order)
                    }

                    // Pass userId to the adapter
                    recyclerView.adapter = RecentOrdersAdapter(ordersList, intent.getIntExtra("user_id", -1))
                    recyclerView.visibility = View.VISIBLE
                    emptyStateText.visibility = View.GONE
                } catch (e: JSONException) {
                    Log.e("API_ERROR", "Parsing error: ${e.message}")
                    emptyStateText.text = "Error parsing orders"
                    emptyStateText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            },
            Response.ErrorListener { error ->
                Log.e("API_ERROR", "Failed request: ${error.message}")
                emptyStateText.text = "Failed to fetch orders"
                emptyStateText.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}

class RecentOrdersAdapter(private val orders: List<Map<String, String>>, private val userId: Int) :
    RecyclerView.Adapter<RecentOrdersAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderIdText: TextView = itemView.findViewById(R.id.orderIdText)
        val orderDateText: TextView = itemView.findViewById(R.id.orderDateText)
        val orderTotalText: TextView = itemView.findViewById(R.id.orderTotalText)
        val orderStatusText: TextView = itemView.findViewById(R.id.orderStatusText)
        val viewOrdersButton: MaterialButton = itemView.findViewById(R.id.viewOrdersButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_orders, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        val orderId = order["id"]?.toIntOrNull() ?: -1

        // Parse the timestamp from the database
        val createdAt = order["created_at"] ?: ""
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(createdAt) ?: Date()

            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            holder.orderIdText.text = dateFormat.format(date)

            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            holder.orderDateText.text = timeFormat.format(date)
        } catch (e: Exception) {
            holder.orderIdText.text = "Order #$createdAt"
            holder.orderDateText.text = createdAt
        }

        holder.orderTotalText.text = "₱${order["total_price"]}"
        holder.orderStatusText.text = order["status"]

        // Everytime i click this and go to ViewOrders.kt, IT ALWAYS CRASHES!!!!!!!
        holder.viewOrdersButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, ViewOrders::class.java)
            intent.putExtra("user_id", userId)
            intent.putExtra("order_id", orderId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = orders.size
}