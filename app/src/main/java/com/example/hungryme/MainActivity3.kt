package com.example.hungryme

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MainActivity3 : AppCompatActivity() {

    private lateinit var registerBtn: ImageView
    private lateinit var login2: ImageView

    private lateinit var editTextEmail: EditText
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPhoneNumber: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirm: EditText

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main3)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registerBtn = findViewById(R.id.registerBtn)
        login2 = findViewById(R.id.login2)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirm = findViewById(R.id.editTextConfirm)

        progressDialog = ProgressDialog(this)

        registerBtn.setOnClickListener {
            registerUser()
        }

        login2.setOnClickListener {
            goToLoginPage()
        }
    }

    private fun clearUserInputs() {
        editTextEmail.setText("")
        editTextUsername.setText("")
        editTextPhoneNumber.setText("")
        editTextPassword.setText("")
        editTextConfirm.setText("")
    }

    private fun goToLoginPage() {
        val intent = Intent(this, MainActivity2::class.java)
        startActivity(intent)
    }

    private fun registerUser() {
        val email = editTextEmail.text.toString().trim()
        val username = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val confirmPassword = editTextConfirm.text.toString().trim()
        var phoneNumber = editTextPhoneNumber.text.toString().trim()

        phoneNumber = phoneNumber.replace("[^0-9]".toRegex(), "")

        // Email validation pattern (strict)
        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        // Password validation: at least 4 letters, 1 number, and 1 special character
        val passwordPattern = Regex("^(?=(.*[A-Za-z]){4})(?=.*\\d)(?=.*[+.#_{}'@\$!%*?&]).{6,}$")

        when {
            username.isEmpty() -> {
                Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show()
                return
            }
            email.isEmpty() -> {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return
            }
            !email.matches(emailPattern) -> {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return
            }
            phoneNumber.isEmpty() -> {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
                return
            }
            phoneNumber.length !in 10..15 -> {  // Ensure phone number is between 10 to 15 digits
                Toast.makeText(this, "Phone number must be between 10 to 15 digits", Toast.LENGTH_SHORT).show()
                return
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
                return
            }
            !password.matches(passwordPattern) -> {
                Toast.makeText(this, "Password must have at least 4 letters, 1 number, and 1 special character", Toast.LENGTH_SHORT).show()
                return
            }
            confirmPassword.isEmpty() -> {
                Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show()
                return
            }
            password != confirmPassword -> {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // If everything is valid, proceed with registration
        Log.d("RegisterDebug", "Sending Data: Username=$username, Email=$email, Password=$password, Phone=$phoneNumber")

        progressDialog.setMessage("Registering user...")
        progressDialog.show()

        val stringRequest = object : StringRequest(
            Request.Method.POST,
            Constants.URL_REGISTER,
            Response.Listener { response ->
                progressDialog.dismiss()
                Log.d("RegisterDebug", "Response: $response")

                try {
                    val jsonObject = JSONObject(response)
                    val message = jsonObject.getString("message")
                    val error = jsonObject.getBoolean("error")
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                    if (!error) { clearUserInputs() }
                }
                catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, "Error parsing response", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                progressDialog.dismiss()
                Log.e("RegisterDebug", "Error: ${error.message}")
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show()
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "username" to username,
                    "email" to email,
                    "password" to password,
                    "phone_number" to phoneNumber
                )
            }
        }

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest)
    }
}
