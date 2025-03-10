package com.example.hungryme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity5 : AppCompatActivity() {
    lateinit var menu : ImageView
    lateinit var account : ImageView
    lateinit var info : ImageView
    lateinit var home : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main5)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        menu = findViewById(R.id.menu)
        account = findViewById(R.id.account)
        info = findViewById(R.id.info)
        home = findViewById(R.id.home)


        account.setOnClickListener {
            val user = intent.getStringExtra("user")
            val intent = Intent(this, MainActivity6::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }

        home.setOnClickListener {
            val user = intent.getStringExtra("user")
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }

        menu.setOnClickListener {
            val user = intent.getStringExtra("user")
            val intent = Intent(this, MainActivity7::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }

        info.setOnClickListener {
            val user = intent.getStringExtra("user")
            val intent = Intent(this, MainActivity8::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }
    }
}