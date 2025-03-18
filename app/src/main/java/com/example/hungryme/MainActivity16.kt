package com.example.hungryme

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MainActivity16 : AppCompatActivity() {

    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var backToLoginPage: ImageView
    private lateinit var updatePassword: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main16)

        password = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.confirmPassword)
        backToLoginPage = findViewById(R.id.backToLoginPage)
        updatePassword = findViewById(R.id.updatePassword)

        val userEmail = intent.getStringExtra("email") ?: ""

        // Back to login alert
        backToLoginPage.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Leave This Page?")
                .setMessage("Are you sure you want to leave? Any progress will be lost.")
                .setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(this, MainActivity2::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Update password
        updatePassword.setOnClickListener {
            val newPassword = password.text.toString().trim()
            val confirmPasswordText = confirmPassword.text.toString().trim()

            if (newPassword.isEmpty() || confirmPasswordText.isEmpty()) {
                Toast.makeText(this, "Password fields cannot be empty.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passwordRegex = Regex("^(?=.*[0-9])(?=.*[!@#\$%^_&*(),.?\":{}|<>]).{6,}$")

            if (!passwordRegex.matches(newPassword)) {
                Toast.makeText(
                    this,
                    "Password must have at least 6 characters, 1 number, and 1 special character.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPasswordText) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Confirmation dialog before updating password
            AlertDialog.Builder(this)
                .setTitle("Update Password?")
                .setMessage("Are you sure you want to update your password?")
                .setPositiveButton("Yes") { _, _ ->
                    updateUserPassword(userEmail, newPassword)
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun updateUserPassword(email: String, password: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Updating Password...")
        progressDialog.show()

        val request = object : StringRequest(
            Request.Method.POST,
            Constants.URL_UPDATE_PASS,
            Response.Listener { response ->
                progressDialog.dismiss()
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        Toast.makeText(this, "Password updated successfully.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity17::class.java))
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error updating password.", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to connect to server.", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["new_password"] = password
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}
