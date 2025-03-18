package com.example.hungryme

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity15 : AppCompatActivity() {

    lateinit var backToLoginPage : ImageView
    lateinit var resetPassword : FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main15)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        backToLoginPage = findViewById(R.id.backToLoginPage)
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
                .setNegativeButton("No", null) // Do nothing if "No" is clicked
                .show()
        }

        // This is the email coming from MainActivity4:
        val yourEmail2 = intent.getStringExtra("email") ?: "your email"

        resetPassword = findViewById(R.id.resetPassword)
        resetPassword.setOnClickListener {
            val intent = Intent(this, MainActivity16::class.java)
            intent.putExtra("email", yourEmail2)
            startActivity(intent)
        }
    }
}