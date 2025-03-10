package com.example.hungryme

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity11 : AppCompatActivity() {
    private lateinit var backPage: ImageView
    private lateinit var checkOut: Button
    private lateinit var checkOrder: FrameLayout
    private lateinit var pushDown: ImageView

    private val itemCounts = IntArray(5) { 0 } // Count of each item
    private val itemPrices = listOf(111, 111, 122, 100, 162) // Prices of each item
    private val itemNames = listOf("Pizza Combo Meal C w/ Regular Drink", "2 pcs Winner Wings Barbeque Value Meal w/ Drink", "Hawaiian Overload Pizzawrap Value Meal w/ Drink", "Snack Plate Lasagna Supreme Solo", "Hawaiian Overload")
    private val itemImages = listOf(R.drawable.pizza_combo_meal_c, R.drawable.winner_wings_barbeque, R.drawable.hawaiian_overload_2, R.drawable.snack_plate_lasagna_supreme, R.drawable.hawaiian_overload)

    private lateinit var valueTextViews: List<TextView>
    private lateinit var addButtons: List<ImageView>
    private lateinit var subtractButtons: List<ImageView>

    private lateinit var foodSelectionLayout: LinearLayout
    private var selectedItemIndex = -1 // To keep track of the selected item (default none)

    private lateinit var addCart: ImageView
    private lateinit var total: TextView
    private var totalPrice = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main11)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backPage = findViewById(R.id.backPage)
        checkOut = findViewById(R.id.checkOut)
        checkOrder = findViewById(R.id.checkOrder)
        pushDown = findViewById(R.id.pushDown)

        foodSelectionLayout = findViewById(R.id.foodSelectionLayout)
        addCart = findViewById(R.id.addCart)
        total = findViewById(R.id.total)

        valueTextViews = listOf(
            findViewById(R.id.value1),
            findViewById(R.id.value2),
            findViewById(R.id.value3),
            findViewById(R.id.value4),
            findViewById(R.id.value5)
        )

        addButtons = listOf(
            findViewById(R.id.add1),
            findViewById(R.id.add2),
            findViewById(R.id.add3),
            findViewById(R.id.add4),
            findViewById(R.id.add5)
        )

        subtractButtons = listOf(
            findViewById(R.id.subtract1),
            findViewById(R.id.subtract2),
            findViewById(R.id.subtract3),
            findViewById(R.id.subtract4),
            findViewById(R.id.subtract5)
        )

        backPage.setOnClickListener {
            startActivity(Intent(this, MainActivity7::class.java))
        }

        for (i in addButtons.indices) {
            addButtons[i].setOnClickListener { updateCount(i, 1) }
            subtractButtons[i].setOnClickListener { updateCount(i, -1) }
        }

        checkOut.setOnClickListener {
            toggleOrderView(true)
        }

        pushDown.setOnClickListener {
            toggleOrderView(false)
        }

        addCart.setOnClickListener {
            if (totalPrice <= 0) {
                Toast.makeText(this, "No items added yet.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, MainActivity13::class.java)

                val selectedItems = itemCounts.indices
                    .filter { itemCounts[it] > 0 }
                    .map { index ->
                        Bundle().apply {
                            putString("name", itemNames[index])
                            putInt("quantity", itemCounts[index])
                            putInt("price", itemPrices[index])
                            putInt("image", itemImages[index])
                        }
                    }

                intent.putParcelableArrayListExtra("cartItems", ArrayList(selectedItems))
                intent.putExtra("totalPrice", totalPrice)

                startActivity(intent)
            }
        }

        for (i in 0 until foodSelectionLayout.childCount) {
            val button = foodSelectionLayout.getChildAt(i) as? Button
            button?.setOnClickListener {
                selectedItemIndex = i
            }
        }
    }

    private fun updateCount(index: Int, delta: Int) {
        if (index !in itemCounts.indices) return

        val newValue = (itemCounts[index] + delta).coerceIn(0, 10)

        if (newValue == itemCounts[index]) {
            val message = if (delta > 0) "You reached the maximum of 10 items!" else "There are no items to be removed."
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            return
        }

        itemCounts[index] = newValue
        valueTextViews[index].text = newValue.toString()

        updateTotalPrice()
        selectedItemIndex = index
        updateFoodSelection(index, newValue)
    }

    private fun updateTotalPrice() {
        totalPrice = itemCounts.indices.sumOf { itemCounts[it] * itemPrices[it] }
        total.text = "₱$totalPrice.00"
    }

    private fun updateFoodSelection(index: Int, newValue: Int) {
        if (index !in itemNames.indices) return

        if (newValue == 0) {
            val itemButton = foodSelectionLayout.findViewWithTag<Button>("item$index")
            itemButton?.let { foodSelectionLayout.removeView(it) }
        } else {
            var itemButton = foodSelectionLayout.findViewWithTag<Button>("item$index")
            if (itemButton == null) {
                itemButton = Button(this).apply {
                    text = itemNames[index]
                    tag = "item$index"
                    setOnClickListener { selectedItemIndex = index }
                }
                foodSelectionLayout.addView(itemButton)
            }
        }
    }

    private fun toggleOrderView(show: Boolean) {
        val animator = ObjectAnimator.ofFloat(
            checkOrder, "translationY", if (show) checkOrder.height.toFloat() else 0f, if (show) 0f else checkOrder.height.toFloat()
        )
        animator.duration = 300
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                checkOrder.visibility = if (show) View.VISIBLE else View.GONE
                if (!show) checkOut.visibility = View.VISIBLE
            }
        })
        checkOrder.visibility = View.VISIBLE
        checkOut.visibility = if (show) View.GONE else View.VISIBLE
        animator.start()
    }
}
