package com.example.hungryme

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
        fetchOrders(userId.toString())
    }

    private fun fetchOrders(userId: String) {
        val url = "${Constants.URL_FETCH_ORDERS}?user_id=$userId"
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

                    recyclerView.adapter = RecentOrdersAdapter(ordersList)
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

class RecentOrdersAdapter(private val orders: List<Map<String, String>>) :
    RecyclerView.Adapter<RecentOrdersAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderIdText: TextView = itemView.findViewById(R.id.orderIdText)
        val orderDateText: TextView = itemView.findViewById(R.id.orderDateText)
        val orderTotalText: TextView = itemView.findViewById(R.id.orderTotalText)
        val orderStatusText: TextView = itemView.findViewById(R.id.orderStatusText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_orders, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        holder.orderIdText.text = "Order #${order["id"]}"
        holder.orderDateText.text = order["created_at"]
        holder.orderTotalText.text = "₱${order["total_price"]}"
        holder.orderStatusText.text = order["status"]
    }

    override fun getItemCount(): Int = orders.size
}
