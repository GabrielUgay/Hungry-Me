package com.example.hungryme

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // What i want is that after clicking each item, you will go to this page and then the user will see the item in a larger image
        val imageFile = findViewById<ImageView>(R.id.imageFile)
        val itemName = findViewById<TextView>(R.id.itemName)
        val itemDesc = findViewById<TextView>(R.id.itemDesc)

        val minusGreen = findViewById<ImageView>(R.id.minusGreen)
        val plusGreen = findViewById<ImageView>(R.id.plusGreen)
        val portion = findViewById<TextView>(R.id.portion)

        val totalPrice = findViewById<TextView>(R.id.totalPrice)
        var numberItems = 0

        itemName.text = intent.getStringExtra("item_name")
        val imageResId = resources.getIdentifier(intent.getStringExtra("item_file"), "drawable", packageName)
        imageFile.setImageResource(imageResId)


        minusGreen.setOnClickListener {
            if (numberItems > 0) {
                numberItems--
                portion.text = numberItems.toString()
            }
        }
        plusGreen.setOnClickListener {
            if (numberItems < 10) {
                numberItems++
                portion.text = numberItems.toString()
            }
        }
    }
}