package com.example.hungryme

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.security.MessageDigest

class ProfileActivity : AppCompatActivity() {

    lateinit var editTextName: EditText
    lateinit var editTextEmail: EditText
    lateinit var editTextDeliveryAddress: EditText



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.statusBarColor = Color.GREEN
        setContentView(R.layout.activity_profile)

        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextDeliveryAddress = findViewById(R.id.editTextDeliveryAddress)

        val backToItems = findViewById<ImageView>(R.id.backToItems)
        backToItems.setOnClickListener {
            finish()
        }

        val user = intent.getStringExtra("user")
        val userId = intent.getIntExtra("user_id", -1)

        Toast.makeText(this, "$user - $userId", Toast.LENGTH_SHORT).show()

        // Fetch profile info from the server
        fetchProfileInfo(userId)
    }


    private fun fetchProfileInfo(userId: Int) {
        val url = "${Constants.URL_PROFILE_INFO}?user_id=$userId" // Replace with your server URL

        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                if (response.getBoolean("success")) {
                    editTextName.setText(response.getString("username"))
                    editTextEmail.setText(response.getString("email"))
                    editTextDeliveryAddress.setText(response.getString("delivery_address"))
                } else {
                    Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error fetching profile: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}