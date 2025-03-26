package com.example.hungryme

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.Manifest
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Cache
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.io.ByteArrayOutputStream

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

        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextDeliveryAddress = findViewById(R.id.editTextDeliveryAddress)

        val backToItems = findViewById<ImageView>(R.id.backToItems)
        backToItems.setOnClickListener {
            finish()
        }

        val user = intent.getStringExtra("user")
        val userId = intent.getIntExtra("user_id", -1)

        val updateProfile = findViewById<MaterialButton>(R.id.updateProfile)
        updateProfile.setOnClickListener {
            updateProfile(userId)
        }

        val logOutBtn = findViewById<MaterialButton>(R.id.logOutBtn)
        logOutBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    // Clear stored user session
                    SharedPrefManager.getInstance(applicationContext).logout()

                    // Go back to the first page (assuming MainActivity2 is the login page)
                    val intent = Intent(this, MainActivity2::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }


        fetchProfileInfo(userId)
    }

    override fun onStart() {
        super.onStart()
        val userId = intent.getIntExtra("user_id", -1)
        if (userId != -1) {
            fetchProfileInfo(userId)
        }
    }

    private fun fetchProfileInfo(userId: Int) {
        val url = "${Constants.URL_PROFILE_INFO}?user_id=$userId"

        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET, url, null,
            { response ->
                Log.d("ProfileFetch", "Response: $response")
                if (response.getBoolean("success")) {
                    val username = response.getString("username")
                    val email = response.getString("email")
                    val deliveryAddress = response.getString("delivery_address")
                    editTextName.setText(username)
                    editTextEmail.setText(email)
                    editTextDeliveryAddress.setText(deliveryAddress)
                    Log.d("ProfileFetch", "Updated UI: $username, $email, $deliveryAddress")
                } else {
                    Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error fetching profile: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("ProfileFetch", "Error: ${error.message}")
            }
        ) {
            override fun getCacheEntry(): Cache.Entry? {
                return null
            }
        }

        requestQueue.add(jsonObjectRequest)
    }

    private fun updateProfile(userId: Int) {
        val url = Constants.URL_UPDATE_PROFILE

        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Method.POST,
            url,
            { response ->
                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                if (success) {
                    fetchProfileInfo(userId)
                }
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error updating profile: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = userId.toString()
                params["username"] = editTextName.text.toString()
                params["email"] = editTextEmail.text.toString()
                params["delivery_address"] = editTextDeliveryAddress.text.toString()
                return params
            }
        }

        requestQueue.add(stringRequest)
    }
}