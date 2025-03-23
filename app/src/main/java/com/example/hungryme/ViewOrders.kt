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
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ViewOrders : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_orders)

        recyclerView = findViewById(R.id.viewOrdersRecyclerView) // Updated to match your XML
        recyclerView.layoutManager = LinearLayoutManager(this)
        emptyStateText = findViewById(R.id.emptyStateText)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        val userId = intent.getIntExtra("user_id", -1)
        val orderId = intent.getIntExtra("order_id", -1)

        Log.d("ViewOrders", "user_id: $userId, order_id: $orderId") // Debug Intent extras

        if (userId == -1 || orderId == -1) {
            emptyStateText.text = "Invalid user or order ID"
            emptyStateText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }

        fetchOrderItems(userId.toString(), orderId.toString())
    }

    private fun fetchOrderItems(userId: String, orderId: String) {
        val url = "${Constants.URL_VIEW_ORDERS}?user_id=$userId&order_id=$orderId"
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

                    val itemsArray = response.getJSONArray("order_items")
                    val orderItemsList = mutableListOf<Map<String, String>>()

                    for (i in 0 until itemsArray.length()) {
                        val itemJson = itemsArray.getJSONObject(i)
                        val item = mapOf(
                            "item_name" to itemJson.getString("item_name"),
                            "quantity" to itemJson.getString("quantity"),
                            "subtotal" to itemJson.getString("subtotal"),
                            "created_at" to itemJson.getString("created_at")
                        )
                        orderItemsList.add(item)
                    }

                    recyclerView.adapter = OrderItemsAdapter(orderItemsList)
                    recyclerView.visibility = View.VISIBLE
                    emptyStateText.visibility = View.GONE
                } catch (e: JSONException) {
                    Log.e("API_ERROR", "Parsing error: ${e.message}")
                    emptyStateText.text = "Error parsing order items"
                    emptyStateText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            },
            Response.ErrorListener { error ->
                Log.e("API_ERROR", "Failed request: ${error.message}")
                emptyStateText.text = "Failed to fetch order items: ${error.message}"
                emptyStateText.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}

class OrderItemsAdapter(private val orderItems: List<Map<String, String>>) :
    RecyclerView.Adapter<OrderItemsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderNameText: TextView = itemView.findViewById(R.id.orderNameText)
        val orderDateText: TextView = itemView.findViewById(R.id.orderDateText)
        val orderQuantityText: TextView = itemView.findViewById(R.id.orderQuantityText)
        val orderPriceText: TextView = itemView.findViewById(R.id.orderPriceText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_orders, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = orderItems[position]

        holder.orderNameText.text = item["item_name"]

        val createdAt = item["created_at"] ?: ""
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(createdAt) ?: Date()
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            holder.orderDateText.text = dateFormat.format(date)
        } catch (e: Exception) {
            holder.orderDateText.text = createdAt
        }

        holder.orderQuantityText.text = "Quantity: ${item["quantity"]}"
        holder.orderPriceText.text = "₱${String.format("%.2f", item["subtotal"]?.toFloatOrNull() ?: 0f)}"
    }

    override fun getItemCount(): Int = orderItems.size
}