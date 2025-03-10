package com.example.hungryme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class MainActivity7 : AppCompatActivity() {
    lateinit var back : ImageView
    lateinit var mangInasal : LinearLayout
    lateinit var jollibee : LinearLayout
    lateinit var greenwichLinearLayout: LinearLayout

    lateinit var mangInasalCart : FrameLayout
    lateinit var jollibeeCart : FrameLayout
    lateinit var greenwichCart : FrameLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main7)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        back = findViewById(R.id.back)

        // Choose any restaurants here:
        mangInasal = findViewById(R.id.mangInasal)
        jollibee = findViewById(R.id.jollibee)
        greenwichLinearLayout = findViewById(R.id.greenwichLinearLayout)

        mangInasalCart = findViewById(R.id.mangInasalCart)
        jollibeeCart = findViewById(R.id.jollibeeCart)
        greenwichCart = findViewById(R.id.greenwichCart)

        back.setOnClickListener {
            val user = intent.getStringExtra("user")
            val intent = Intent(this, MainActivity5::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }

        mangInasal.setOnClickListener { goToPageNine() }
        jollibee.setOnClickListener { goToPageTen() }
        greenwichLinearLayout.setOnClickListener { goToPageEleven() }

        mangInasalCart.setOnClickListener { goToCartPage("Mang Inasal") }
        jollibeeCart.setOnClickListener { goToCartPage("Jollibee") }
        greenwichCart.setOnClickListener { goToCartPage("Greenwich") }

    }


    private fun goToCartPage(restaurant: String) {
        val user = intent.getStringExtra("user")

        val url = "${Constants.URL_GET_USER_ID}?username=$user"
        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    Log.d("CartActivity", "Raw API Response: $response")

                    val jsonObject = JSONObject(response.trim())
                    val userId = jsonObject.optInt("user_id")

                    if (userId == -1) {
                        Log.e("CartActivity", "Invalid response: Missing 'userId'")
                        Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show()
                        return@StringRequest
                    }

                    Log.d("CartActivity", "Fetched UserID: $userId")

                    val intent = Intent(this, CartActivity::class.java).apply {
                        putExtra("user", user)
                        putExtra("restaurant", restaurant)
                        putExtra("user_id", userId)
                    }
                    startActivity(intent)
                } catch (e: JSONException) {
                    Log.e("CartActivity", "JSON Parsing error: ${e.message}")
                    Toast.makeText(this, "Error parsing user data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("CartActivity", "Volley error: ${error.message}")
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_LONG).show()

                error.networkResponse?.let {
                    Log.e("CartActivity", "Error Response Code: ${it.statusCode}")
                    Log.e("CartActivity", "Error Response Data: ${String(it.data)}")
                }
            }
        )

        requestQueue.add(stringRequest)
    }





    private fun goToPageNine() {
        val user = intent.getStringExtra("user")
        val intent = Intent(this, MainTest::class.java)
        intent.putExtra("restaurant", "Mang Inasal")
        intent.putExtra("user", user)
        startActivity(intent)
    }

    private fun goToPageTen() {
        val user = intent.getStringExtra("user")
        val intent = Intent(this, MainTest::class.java)
        intent.putExtra("restaurant", "Jollibee")
        intent.putExtra("user", user)
        startActivity(intent)
    }

    private fun goToPageEleven() {
        val user = intent.getStringExtra("user")
        val intent = Intent(this, MainTest::class.java)
        intent.putExtra("restaurant", "Greenwich")
        intent.putExtra("user", user)
        startActivity(intent)
    }
}
